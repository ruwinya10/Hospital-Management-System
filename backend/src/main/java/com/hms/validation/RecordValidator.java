package com.hms.validation;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hms.model.*;
import jakarta.persistence.EntityManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.regex.Pattern;

@Service public class RecordValidator {
 private final JsonNode rules;private final ObjectMapper json;private final EntityManager em;
 private static final ZoneId ZONE=ZoneId.of("Asia/Colombo");
 private static final Map<String,Class<? extends BaseRecord>> REFERENCES=Map.of("patientId",Patient.class,"doctorId",Doctor.class,"departmentId",Department.class,"appointmentId",Appointment.class,"billId",Bill.class,"employeeId",Employee.class);
 public RecordValidator(ObjectMapper json,EntityManager em)throws java.io.IOException{this.json=json;this.em=em;try(var input=new ClassPathResource("formRules.json").getInputStream()){rules=json.readTree(input);}}
 private String label(String key){if(key.isEmpty())return "Field";String spaced=key.replaceAll("([A-Z])"," $1");return rules.path("fields").path(key).path("label").asText(Character.toUpperCase(spaced.charAt(0))+spaced.substring(1));}
 private String text(JsonNode d,String k){return d.path(k).asText("").trim();}
 private boolean contains(JsonNode array,String value){for(JsonNode n:array)if(n.asText().equals(value))return true;return false;}
 private BigDecimal number(JsonNode d,String k){try{return new BigDecimal(text(d,k));}catch(Exception e){return BigDecimal.ZERO;}}
 private LocalDate date(String value){return LocalDate.parse(value,DateTimeFormatter.ISO_LOCAL_DATE.withResolverStyle(ResolverStyle.STRICT));}
 private LocalDateTime timestamp(String value){return LocalDateTime.parse(value,DateTimeFormatter.ISO_LOCAL_DATE_TIME);}
 @Transactional(readOnly=true) public void validate(BaseRecord item,String module){
  Map<String,String> errors=new LinkedHashMap<>();JsonNode config=rules.path("modules").path(module);ObjectNode details;
  try{JsonNode parsed=json.readTree(item.details==null?"{}":item.details);if(parsed==null||!parsed.isObject())throw new IllegalArgumentException();details=(ObjectNode)parsed;}catch(Exception e){throw new FormValidationException(Map.of("name","Record details must be a valid JSON object."));}
  if(module.equals("patients"))details.remove("departmentId");
  for(String generated:List.of("patientNumber","staffNumber","medicineCode"))details.remove(generated);
  if(module.equals("doctors")&&text(details,"schedule").startsWith("[")){
   try{JsonNode slots=json.readTree(text(details,"schedule"));if(!slots.isArray())throw new IllegalArgumentException();for(JsonNode slot:slots){if(!List.of("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday").contains(text(slot,"day")))throw new IllegalArgumentException();String start=text(slot,"start"),end=text(slot,"end");if(!start.matches("(?:[01]\\d|2[0-3]):[0-5]\\d")||!end.matches("(?:[01]\\d|2[0-3]):[0-5]\\d")||!LocalTime.parse(end).isAfter(LocalTime.parse(start)))throw new IllegalArgumentException();}}
   catch(Exception e){errors.put("schedule","Choose a day and an end time after the start time for every availability.");}
  }
  item.name=item.name==null?"":item.name.trim();item.status=item.status==null?"":item.status.trim().toUpperCase(Locale.ROOT);
  if(item.name.isEmpty()||item.name.length()>200)errors.put("name","Name / Title is required and must not exceed 200 characters.");
  if(config.path("humanName").asBoolean()&&!item.name.matches("[\\p{L}\\p{M} .’'-]{2,120}"))errors.put("name","Enter a person's name using 2–120 letters, spaces, apostrophes, dots or hyphens.");
  if(!contains(config.path("statuses"),item.status))errors.put("status","Choose a valid status for this record.");
  Set<String> keys=new LinkedHashSet<>();config.path("required").forEach(n->keys.add(n.asText()));details.fieldNames().forEachRemaining(keys::add);
  for(String key:keys){JsonNode rule=rules.path("fields").path(key);if(key.equals("name")||key.equals("status")){errors.put("name","Record details cannot contain reserved name or status fields.");continue;}String value=text(details,key),name=label(key);boolean required=contains(config.path("required"),key)&&!(key.equals("departmentId")&&!text(details,"department").isEmpty());
   if(value.isEmpty()){if(required)errors.put(key,name+" is required.");continue;}
   if(details.path(key).isContainerNode()){errors.put(key,name+" must be a single value.");continue;}
   if(value.length()>rule.path("maxLength").asInt(4000)){errors.put(key,name+" is too long (maximum "+rule.path("maxLength").asInt(4000)+" characters).");continue;}
   if(rule.has("pattern")&&!Pattern.matches(rule.path("pattern").asText(),value)){errors.put(key,rule.path("patternMessage").asText("Enter a valid "+name+"."));continue;}
   String type=rule.path("type").asText("text");
   if(type.equals("number")){try{if(!value.matches("\\d+(?:\\.\\d+)?"))throw new IllegalArgumentException();BigDecimal n=new BigDecimal(value);if(rule.path("integer").asBoolean()&&n.stripTrailingZeros().scale()>0)throw new IllegalArgumentException();if(n.compareTo(rule.path("min").decimalValue())<0||n.compareTo(rule.path("max").decimalValue())>0)throw new IllegalArgumentException();if(rule.has("decimals")&&Math.max(0,n.scale())>rule.path("decimals").asInt())throw new IllegalArgumentException();}catch(Exception e){errors.put(key,name+" must be between "+rule.path("min").asText()+" and "+rule.path("max").asText()+(rule.path("integer").asBoolean()?", using whole numbers.":rule.has("decimals")?", with at most "+rule.path("decimals").asInt()+" decimal places.":"."));}}
   if(type.equals("tel")){long digits=value.chars().filter(Character::isDigit).count();if(!value.matches("\\+?[0-9 ()-]+")||digits<7||digits>15)errors.put(key,name+" must contain 7–15 digits; +, spaces, parentheses and hyphens are allowed.");}
   if(type.equals("email")&&!value.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))errors.put(key,"Enter a valid email address, such as name@example.com.");
   if(rule.has("options")){String match=null;for(JsonNode option:rule.path("options"))if(option.asText().equalsIgnoreCase(value))match=option.asText();if(match==null)errors.put(key,"Choose a valid "+name+" from the list.");else value=match;}
   try{
    if(type.equals("date")){if(!value.matches("\\d{4}-\\d{2}-\\d{2}"))throw new IllegalArgumentException();LocalDate d=date(value);if(rule.path("noFuture").asBoolean()&&d.isAfter(LocalDate.now(ZONE)))errors.put(key,name+" cannot be in the future.");if(key.equals("dateOfBirth")&&d.isBefore(LocalDate.now(ZONE).minusYears(130)))errors.put(key,"Date of birth cannot be more than 130 years ago.");}
    if(type.equals("datetime-local")){LocalDateTime dt=timestamp(value);if(rule.path("noFuture").asBoolean()&&dt.atZone(ZONE).toInstant().isAfter(Instant.now()))errors.put(key,name+" cannot be in the future.");}
    if(type.equals("time")){if(!value.matches("(?:[01]\\d|2[0-3]):[0-5]\\d(?::[0-5]\\d)?"))throw new IllegalArgumentException();LocalTime.parse(value);}
   }catch(Exception e){errors.put(key,"Enter a valid "+name.toLowerCase()+".");}
   if(rule.path("reference").asBoolean()&&!errors.containsKey(key)){try{BaseRecord related=em.find(REFERENCES.get(key),Long.valueOf(value));if(related==null||"ARCHIVED".equals(related.status))errors.put(key,"Choose an existing, active "+key.replace("Id","")+" record.");}catch(Exception e){errors.put(key,"Enter a valid record ID.");}}
   details.put(key,value);
  }
  order(details,"admissionDate","dischargeDate","Discharge date must be on or after admission date.",errors);order(details,"startDate","endDate","End date must be on or after start date.",errors);order(details,"billDate","dueDate","Due date must be on or after bill date.",errors);order(details,"requestedAt","completedAt","Completion must be on or after the request time.",errors);order(details,"checkIn","checkOut","Check-out must be on or after check-in.",errors);
  if(module.equals("attendance")){
   if(item.status.equals("ABSENT")&&(!text(details,"checkIn").isEmpty()||!text(details,"checkOut").isEmpty()))errors.put("checkIn","An absent record cannot have check-in or check-out times.");
   if(!List.of("ABSENT","ARCHIVED").contains(item.status)&&text(details,"checkIn").isEmpty())errors.put("checkIn","Check-in is required for a present employee.");
   if(!text(details,"date").isEmpty()&&text(details,"date").compareTo(LocalDate.now(ZONE).toString())>0)errors.put("date","Attendance date cannot be in the future.");
  }
  if(module.equals("admissions")&&item.status.equals("DISCHARGED")&&text(details,"dischargeDate").isEmpty())errors.put("dischargeDate","Enter the discharge date for a discharged patient.");
  if(module.equals("laboratory-tests")&&item.status.equals("COMPLETED")){if(text(details,"result").isEmpty())errors.put("result","Enter the result before marking the test completed.");if(text(details,"completedAt").isEmpty())errors.put("completedAt","Enter the completion date and time.");}
  if(module.equals("appointments")&&item.status.equals("COMPLETED")&&text(details,"date").compareTo(LocalDate.now(ZONE).toString())>0)errors.put("date","A future appointment cannot be marked completed.");
  if(module.equals("payments")&&!text(details,"method").equals("CASH")&&text(details,"reference").isEmpty())errors.put("reference","Enter a reference for a non-cash payment.");
  List<String> charges=List.of("consultationCharge","laboratoryCharge","pharmacyCharge","admissionCharge");if(module.equals("bills")&&charges.stream().anyMatch(k->!text(details,k).isEmpty())){BigDecimal total=charges.stream().map(k->number(details,k)).reduce(BigDecimal.ZERO,BigDecimal::add);if(number(details,"total").compareTo(total)!=0)errors.put("total","Total must equal all four service charges combined.");}
  if(errors.isEmpty())checkDatabase(item,module,details,config,errors);
  if(!errors.isEmpty())throw new FormValidationException(errors);
  item.details=details.toString();
 }
 private void order(JsonNode d,String start,String end,String message,Map<String,String> errors){if(!text(d,start).isEmpty()&&!text(d,end).isEmpty()&&!errors.containsKey(start)&&!errors.containsKey(end)&&text(d,end).compareTo(text(d,start))<0)errors.put(end,message);}
 private JsonNode details(BaseRecord r){try{JsonNode node=json.readTree(r.details);return node!=null&&node.isObject()?node:json.createObjectNode();}catch(Exception e){return json.createObjectNode();}}
 private void checkDatabase(BaseRecord item,String module,JsonNode d,JsonNode config,Map<String,String> errors){
  if(config.path("unique").size()>0||module.equals("attendance")||module.equals("appointments")){
   List<? extends BaseRecord> records=em.createQuery("select r from "+item.getClass().getSimpleName()+" r",item.getClass()).getResultList();
   for(BaseRecord existing:records){if(Objects.equals(existing.id,item.id))continue;JsonNode other=details(existing);
    for(JsonNode unique:config.path("unique")){String key=unique.asText();if(!text(d,key).isEmpty()&&text(d,key).equalsIgnoreCase(text(other,key)))errors.put(key,label(key)+" already exists. Use a unique value.");}
    if("ARCHIVED".equals(existing.status))continue;
    if(module.equals("attendance")&&text(d,"employeeId").equals(text(other,"employeeId"))&&text(d,"date").equals(text(other,"date")))errors.put("date","An attendance record already exists for this employee and date.");
    if(module.equals("appointments")&&!List.of("CANCELLED","COMPLETED","ARCHIVED").contains(item.status)&&!List.of("CANCELLED","COMPLETED","ARCHIVED").contains(existing.status)&&text(d,"doctorId").equals(text(other,"doctorId"))&&text(d,"date").equals(text(other,"date"))&&text(d,"time").substring(0,5).equals(text(other,"time").length()>=5?text(other,"time").substring(0,5):""))errors.put("time","This doctor already has an appointment at the selected date and time.");
   }
  }
  if(module.equals("payments")&&!List.of("CANCELLED","REJECTED","ARCHIVED","PENDING").contains(item.status)){
   Bill bill=em.find(Bill.class,Long.valueOf(text(d,"billId")));BigDecimal paid=BigDecimal.ZERO;
   for(Payment payment:em.createQuery("select p from Payment p",Payment.class).getResultList())if(!Objects.equals(payment.id,item.id)&&List.of("ACTIVE","COMPLETED","PAID","APPROVED").contains(payment.status)){JsonNode other=details(payment);if(text(other,"billId").equals(text(d,"billId")))paid=paid.add(number(other,"amount"));}
   if(bill!=null&&number(d,"amount").compareTo(number(details(bill),"total").subtract(paid))>0)errors.put("amount","Payment exceeds the outstanding bill balance.");
  }
 }
}
