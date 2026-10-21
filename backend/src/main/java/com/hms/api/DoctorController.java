package com.hms.api;
import com.hms.model.*;import com.hms.repo.*;
import org.springframework.web.bind.annotation.*;import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/doctors") public class DoctorController {
 @org.springframework.beans.factory.annotation.Autowired private com.hms.validation.RecordValidator recordValidator;
 private final DoctorRepository repo;private final AuditLogRepository audit;
 public DoctorController(DoctorRepository r,AuditLogRepository a){repo=r;audit=a;}
 private void guard(Authentication a){if(!(a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADMIN")) || a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_RECEPTIONIST"))))throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 @GetMapping public List<Doctor> all(Authentication a){guard(a);return repo.findAll();}
 @GetMapping("/{id}") public Doctor one(@PathVariable Long id,Authentication a){guard(a);return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @PostMapping public Doctor add(@RequestBody Doctor item,Authentication a){guard(a);item.id=null;recordValidator.validate(item,"doctors");if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");item.id=null;Doctor saved=repo.save(item);audit.save(new AuditLog(a.getName(),"CREATE","Doctor",saved.id.toString()));return saved;}
 @PutMapping("/{id}") public Doctor update(@PathVariable Long id,@RequestBody Doctor item,Authentication a){guard(a);Doctor existing=one(id,a);item.id=id;recordValidator.validate(item,"doctors");if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");existing.name=item.name;existing.status=item.status;existing.details=item.details;Doctor saved=repo.save(existing);audit.save(new AuditLog(a.getName(),"UPDATE","Doctor",id.toString()));return saved;}
 @DeleteMapping("/{id}") public void delete(@PathVariable Long id,Authentication a){guard(a);Doctor existing=one(id,a);existing.status="ARCHIVED";repo.save(existing);audit.save(new AuditLog(a.getName(),"ARCHIVE","Doctor",id.toString()));}
}