package com.hms.reporting;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hms.model.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
@Service public class PharmacyMovementService {
 private final EntityManager em;private final ObjectMapper json;private final AnalyticsService reports;
 public PharmacyMovementService(EntityManager em,ObjectMapper json,AnalyticsService reports){this.em=em;this.json=json;this.reports=reports;}
 public record MovementInput(@NotNull Long medicineId,@NotBlank String type,@NotNull @DecimalMin("0.001") @DecimalMax("999999999") @Digits(integer=9,fraction=3) BigDecimal quantity,@DecimalMin("0") @DecimalMax("999999999") @Digits(integer=9,fraction=2) BigDecimal unitPrice,Long patientId,@Size(max=200) String reference,LocalDateTime occurredAt){}
 @Transactional public PharmacyMovement record(MovementInput input,Authentication a){
  reports.guard(a,"pharmacy");
  if(!List.of("RECEIPT","DISPENSE","ADJUST_IN","ADJUST_OUT").contains(input.type()))throw AnalyticsService.bad("Invalid movement type");
  PharmacyItem item=em.find(PharmacyItem.class,input.medicineId(),LockModeType.PESSIMISTIC_WRITE);
  if(item==null||"ARCHIVED".equals(item.status))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Medicine not found");
  if(input.patientId()!=null&&em.find(Patient.class,input.patientId())==null)throw AnalyticsService.bad("Patient record does not exist");
  ObjectNode details;try{JsonNode node=json.readTree(item.details);if(!node.isObject())throw new IllegalArgumentException();details=(ObjectNode)node;}catch(Exception e){throw AnalyticsService.bad("Medicine details contain invalid JSON");}
  BigDecimal stock,price;try{stock=new BigDecimal(details.path("quantity").asText("0"));price=input.unitPrice()!=null?input.unitPrice():new BigDecimal(details.path("unitPrice").asText("0"));}catch(Exception e){throw AnalyticsService.bad("Medicine stock or price is invalid");}
  if(stock.signum()<0||price.signum()<0)throw AnalyticsService.bad("Stock and price cannot be negative");
  Instant timestamp=input.occurredAt()==null?Instant.now():input.occurredAt().atZone(AnalyticsService.ZONE).toInstant();
  if(timestamp.isAfter(Instant.now().plusSeconds(60)))throw AnalyticsService.bad("Movement date cannot be in the future");
  if(item.createdAt!=null&&timestamp.isBefore(item.createdAt))throw AnalyticsService.bad("Movement date cannot precede medicine creation");
  LocalDate expiry=AnalyticsService.parseDate(details.path("expiryDate").asText(""));
  if(input.type().equals("DISPENSE")&&(expiry==null||expiry.isBefore(LocalDate.now(AnalyticsService.ZONE))))throw AnalyticsService.bad("An unexpired expiry date is required before dispensing");
  boolean outgoing=input.type().equals("DISPENSE")||input.type().equals("ADJUST_OUT");BigDecimal updated=outgoing?stock.subtract(input.quantity()):stock.add(input.quantity());
  if(updated.signum()<0)throw AnalyticsService.bad("Not enough stock for this movement");
  details.put("quantity",updated);item.details=details.toString();
  PharmacyMovement movement=new PharmacyMovement();movement.medicineId=item.id;movement.type=input.type();movement.quantity=input.quantity();movement.unitPrice=price;movement.occurredAt=timestamp;movement.patientId=input.patientId();movement.reference=input.reference();movement.recordedBy=a.getName();em.persist(movement);em.persist(new AuditLog(a.getName(),"STOCK_"+input.type(),"PharmacyItem",item.id.toString()));return movement;
 }
}
