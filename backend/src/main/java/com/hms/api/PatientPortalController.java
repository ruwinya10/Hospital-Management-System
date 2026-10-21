package com.hms.api;
import com.hms.model.*;
import com.hms.repo.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/portal") public class PatientPortalController {
 private final UserRepository users; private final PatientRepository patients; private final AppointmentRepository appointments; private final MedicalRecordRepository records; private final LaboratoryTestRepository labs; private final BillRepository bills; private final ObjectMapper json;
 public PatientPortalController(UserRepository u,PatientRepository p,AppointmentRepository a,MedicalRecordRepository r,LaboratoryTestRepository l,BillRepository b,ObjectMapper j){users=u;patients=p;appointments=a;records=r;labs=l;bills=b;json=j;}
 @GetMapping public Map<String,Object> portal(Authentication a){
  if(a.getAuthorities().stream().noneMatch(x->x.getAuthority().equals("ROLE_PATIENT")))throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  UserAccount u=users.findByUsername(a.getName()).orElseThrow();
  if(u.patientId==null)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"No patient record linked");
  Patient patient=patients.findById(u.patientId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  return Map.of("patient",patient,"appointments",owned(appointments.findAll(),u.patientId),"medical-records",owned(records.findAll(),u.patientId),"laboratory-tests",owned(labs.findAll(),u.patientId),"bills",owned(bills.findAll(),u.patientId));
 }
 private <T extends BaseRecord> List<T> owned(List<T> rows,Long id){return rows.stream().filter(row->{try{var value=json.readTree(row.details).get("patientId");return value!=null&&id.toString().equals(value.asText());}catch(Exception e){return false;}}).toList();}
}
