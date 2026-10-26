package com.hms.api;

import com.hms.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api/lookups")
public class RecordLookupController {
 private final EntityManager em;private final ObjectMapper json;
 public RecordLookupController(EntityManager em,ObjectMapper json){this.em=em;this.json=json;}
 @GetMapping("/{type}") public List<Map<String,Object>> lookup(@PathVariable String type,Authentication auth){
  Class<? extends BaseRecord> entity=switch(type){case "patients"->Patient.class;case "doctors"->Doctor.class;case "departments"->Department.class;case "appointments"->Appointment.class;case "bills"->Bill.class;case "employees"->Employee.class;default->throw new ResponseStatusException(HttpStatus.NOT_FOUND);};
  Set<String> allowed=switch(type){case "employees","departments"->Set.of("ADMIN","RECEPTIONIST");case "appointments","bills"->Set.of("ADMIN","ACCOUNTANT","RECEPTIONIST");default->Set.of("ADMIN","RECEPTIONIST","DOCTOR","NURSE","LAB","ACCOUNTANT");};
  if(auth.getAuthorities().stream().noneMatch(a->allowed.contains(a.getAuthority().replace("ROLE_",""))))throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  return em.createQuery("select r from "+entity.getSimpleName()+" r where r.status <> 'ARCHIVED'",entity).getResultList().stream().map(r->{Map<String,Object> result=new LinkedHashMap<>();result.put("id",r.id);result.put("recordId",r.getRecordId());result.put("name",r.name);if(r instanceof Doctor){try{result.put("schedule",json.readTree(r.details).path("schedule").asText(""));}catch(Exception ignored){result.put("schedule","");}}return result;}).toList();
 }
}
