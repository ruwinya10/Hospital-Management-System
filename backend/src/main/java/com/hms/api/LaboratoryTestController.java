package com.hms.api;
import com.hms.model.*;import com.hms.repo.*;
import org.springframework.web.bind.annotation.*;import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/laboratory-tests") public class LaboratoryTestController {
 @org.springframework.beans.factory.annotation.Autowired private com.hms.validation.RecordValidator recordValidator;
 private final LaboratoryTestRepository repo;private final AuditLogRepository audit;
 public LaboratoryTestController(LaboratoryTestRepository r,AuditLogRepository a){repo=r;audit=a;}
 private void guard(Authentication a){if(!(a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADMIN")) || a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_DOCTOR")) || a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_LAB"))))throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 @GetMapping public List<LaboratoryTest> all(Authentication a){guard(a);return repo.findAll();}
 @GetMapping("/{id}") public LaboratoryTest one(@PathVariable Long id,Authentication a){guard(a);return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @PostMapping public LaboratoryTest add(@RequestBody LaboratoryTest item,Authentication a){guard(a);item.id=null;recordValidator.validate(item,"laboratory-tests");if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");item.id=null;LaboratoryTest saved=repo.save(item);audit.save(new AuditLog(a.getName(),"CREATE","LaboratoryTest",saved.id.toString()));return saved;}
 @PutMapping("/{id}") public LaboratoryTest update(@PathVariable Long id,@RequestBody LaboratoryTest item,Authentication a){guard(a);LaboratoryTest existing=one(id,a);item.id=id;recordValidator.validate(item,"laboratory-tests");if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");existing.name=item.name;existing.status=item.status;existing.details=item.details;LaboratoryTest saved=repo.save(existing);audit.save(new AuditLog(a.getName(),"UPDATE","LaboratoryTest",id.toString()));return saved;}
 @DeleteMapping("/{id}") public void delete(@PathVariable Long id,Authentication a){guard(a);LaboratoryTest existing=one(id,a);existing.status="ARCHIVED";repo.save(existing);audit.save(new AuditLog(a.getName(),"ARCHIVE","LaboratoryTest",id.toString()));}
}