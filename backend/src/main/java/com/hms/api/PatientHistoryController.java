package com.hms.api;

import com.hms.model.*;
import com.hms.repo.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController @RequestMapping("/api")
public class PatientHistoryController {
 private final EntityManager em;private final ObjectMapper json;private final DocumentRepository documents;private final AuditLogRepository audit;
 public PatientHistoryController(EntityManager em,ObjectMapper json,DocumentRepository documents,AuditLogRepository audit){this.em=em;this.json=json;this.documents=documents;this.audit=audit;}
 private boolean has(Authentication a,String...roles){return a.getAuthorities().stream().anyMatch(r->Arrays.asList(roles).contains(r.getAuthority().replace("ROLE_","")));}
 private void guard(Authentication a){if(!has(a,"ADMIN","DOCTOR","NURSE","RECEPTIONIST"))throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 private Patient patient(Long id){Patient p=em.find(Patient.class,id);if(p==null)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Patient not found");return p;}
 private List<? extends BaseRecord> linked(Class<? extends BaseRecord> type,Long patientId){return em.createQuery("select r from "+type.getSimpleName()+" r where r.status <> 'ARCHIVED' order by r.createdAt desc",type).getResultList().stream().filter(r->{try{return json.readTree(r.details).path("patientId").asText().equals(patientId.toString());}catch(Exception e){return false;}}).toList();}
 @GetMapping("/patients/{id}/history") @Transactional public Map<String,Object> history(@PathVariable Long id,Authentication a){guard(a);patient(id);Map<String,Object> result=new LinkedHashMap<>();List<String> restricted=new ArrayList<>();
  if(has(a,"ADMIN","DOCTOR","NURSE"))result.put("medical-records",linked(MedicalRecord.class,id));else restricted.add("medical-records");
  if(has(a,"ADMIN","RECEPTIONIST","NURSE"))result.put("admissions",linked(Admission.class,id));else restricted.add("admissions");
  if(has(a,"ADMIN","RECEPTIONIST","DOCTOR"))result.put("appointments",linked(Appointment.class,id));else restricted.add("appointments");
  if(has(a,"ADMIN","DOCTOR","LAB"))result.put("laboratory-tests",linked(LaboratoryTest.class,id));else restricted.add("laboratory-tests");
  result.put("documents",linked(Document.class,id));result.put("restricted",restricted);audit.save(new AuditLog(a.getName(),"VIEW_HISTORY","Patient",id.toString(),"Viewed patient history and document list."));return result;
 }
 @PostMapping(value="/patients/{id}/documents",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @Transactional public Document upload(@PathVariable Long id,@RequestParam("file") MultipartFile file,Authentication a)throws java.io.IOException{
  guard(a);patient(id);if(file.isEmpty()||file.getSize()>5*1024*1024)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a non-empty file up to 5 MB.");
  String name=Optional.ofNullable(file.getOriginalFilename()).orElse("").replace('\\','/');name=name.substring(name.lastIndexOf('/')+1);if(name.isBlank()||name.length()>200||name.chars().anyMatch(c->c<32))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a filename of at most 200 characters.");
  String ext=name.substring(name.lastIndexOf('.')+1).toLowerCase(Locale.ROOT);String contentType=switch(ext){case "pdf"->"application/pdf";case "png"->"image/png";case "jpg","jpeg"->"image/jpeg";case "doc"->"application/msword";case "docx"->"application/vnd.openxmlformats-officedocument.wordprocessingml.document";default->throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Supported files: Word (.doc, .docx), PDF, PNG, JPG and JPEG.");};
  byte[] bytes=file.getBytes();boolean valid=switch(ext){case "pdf"->starts(bytes,0x25,0x50,0x44,0x46,0x2d);case "png"->starts(bytes,0x89,0x50,0x4e,0x47,0x0d,0x0a,0x1a,0x0a);case "jpg","jpeg"->starts(bytes,0xff,0xd8,0xff);case "doc"->starts(bytes,0xd0,0xcf,0x11,0xe0,0xa1,0xb1,0x1a,0xe1);case "docx"->isWordZip(bytes);default->false;};
  if(!valid)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"The file contents do not match the selected file type.");
  Document document=new Document();document.name=name;document.content=bytes;document.contentType=contentType;document.fileSize=file.getSize();document.details=json.createObjectNode().put("patientId",id.toString()).put("fileReference","Uploaded document").put("description",name).toString();Document saved=documents.save(document);audit.save(new AuditLog(a.getName(),"UPLOAD","Document",saved.id.toString(),"Uploaded "+name+" for "+patient(id).getRecordId()));return saved;
 }
 private boolean starts(byte[] bytes,int...prefix){if(bytes.length<prefix.length)return false;for(int i=0;i<prefix.length;i++)if((bytes[i]&255)!=prefix[i])return false;return true;}
 private boolean isWordZip(byte[] bytes){try(var zip=new java.util.zip.ZipInputStream(new java.io.ByteArrayInputStream(bytes))){java.util.zip.ZipEntry entry;int count=0;while((entry=zip.getNextEntry())!=null&&count++<2000)if(entry.getName().equals("word/document.xml"))return true;}catch(Exception ignored){}return false;}
 @GetMapping("/documents/{id}/content") @Transactional public ResponseEntity<byte[]> download(@PathVariable Long id,Authentication a){guard(a);Document document=documents.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));if(document.content==null||"ARCHIVED".equals(document.status))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Uploaded file not found");audit.save(new AuditLog(a.getName(),"DOWNLOAD","Document",id.toString(),"Downloaded "+document.name));return ResponseEntity.ok().contentType(MediaType.parseMediaType(document.contentType)).header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(document.name,StandardCharsets.UTF_8).build().toString()).header("X-Content-Type-Options","nosniff").body(document.content);}
}
