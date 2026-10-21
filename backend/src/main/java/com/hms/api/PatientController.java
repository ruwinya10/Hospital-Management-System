package com.hms.api;
import com.hms.model.*;import com.hms.repo.*;
import org.springframework.web.bind.annotation.*;import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/patients") public class PatientController {
 @org.springframework.beans.factory.annotation.Autowired private com.hms.validation.RecordValidator recordValidator;
 private final PatientRepository repo;private final AuditLogRepository audit;
 public PatientController(PatientRepository r,AuditLogRepository a){repo=r;audit=a;}
 private void guard(Authentication a){if(!(a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADMIN")) || a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_RECEPTIONIST")) || a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_DOCTOR")) || a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_NURSE"))))throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 @GetMapping public List<Patient> all(Authentication a){guard(a);return repo.findAll();}
 @GetMapping("/{id}") public Patient one(@PathVariable Long id,Authentication a){guard(a);return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @PostMapping public Patient add(@RequestBody Patient item,Authentication a){guard(a);item.id=null;recordValidator.validate(item,"patients");if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");item.id=null;Patient saved=repo.save(item);audit.save(new AuditLog(a.getName(),"CREATE","Patient",saved.id.toString()));return saved;}
 @PutMapping("/{id}") public Patient update(@PathVariable Long id,@RequestBody Patient item,Authentication a){guard(a);Patient existing=one(id,a);item.id=id;recordValidator.validate(item,"patients");if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");existing.name=item.name;existing.status=item.status;existing.details=item.details;Patient saved=repo.save(existing);audit.save(new AuditLog(a.getName(),"UPDATE","Patient",id.toString()));return saved;}
 @DeleteMapping("/{id}") public void delete(@PathVariable Long id,Authentication a){guard(a);Patient existing=one(id,a);existing.status="ARCHIVED";repo.save(existing);audit.save(new AuditLog(a.getName(),"ARCHIVE","Patient",id.toString()));}
}