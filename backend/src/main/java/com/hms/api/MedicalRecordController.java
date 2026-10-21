package com.hms.api;
import com.hms.model.*;import com.hms.repo.*;
import org.springframework.web.bind.annotation.*;import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/medical-records") public class MedicalRecordController {
 private final MedicalRecordRepository repo;private final AuditLogRepository audit;
 public MedicalRecordController(MedicalRecordRepository r,AuditLogRepository a){repo=r;audit=a;}
 private void guard(Authentication a){if(!(a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADMIN")) || a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_DOCTOR")) || a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_NURSE"))))throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 @GetMapping public List<MedicalRecord> all(Authentication a){guard(a);return repo.findAll();}
 @GetMapping("/{id}") public MedicalRecord one(@PathVariable Long id,Authentication a){guard(a);return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @PostMapping public MedicalRecord add(@RequestBody MedicalRecord item,Authentication a){guard(a);if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");item.id=null;MedicalRecord saved=repo.save(item);audit.save(new AuditLog(a.getName(),"CREATE","MedicalRecord",saved.id.toString()));return saved;}
 @PutMapping("/{id}") public MedicalRecord update(@PathVariable Long id,@RequestBody MedicalRecord item,Authentication a){guard(a);MedicalRecord existing=one(id,a);if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");existing.name=item.name;existing.status=item.status;existing.details=item.details;MedicalRecord saved=repo.save(existing);audit.save(new AuditLog(a.getName(),"UPDATE","MedicalRecord",id.toString()));return saved;}
 @DeleteMapping("/{id}") public void delete(@PathVariable Long id,Authentication a){guard(a);MedicalRecord existing=one(id,a);existing.status="ARCHIVED";repo.save(existing);audit.save(new AuditLog(a.getName(),"ARCHIVE","MedicalRecord",id.toString()));}
}