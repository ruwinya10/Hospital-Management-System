package com.hms.api;
import com.hms.model.*;import com.hms.repo.*;
import org.springframework.web.bind.annotation.*;import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/departments") public class DepartmentController {
 @org.springframework.beans.factory.annotation.Autowired private com.hms.validation.RecordValidator recordValidator;
 private final DepartmentRepository repo;private final AuditLogRepository audit;
 public DepartmentController(DepartmentRepository r,AuditLogRepository a){repo=r;audit=a;}
 private void guard(Authentication a){if(!(a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADMIN"))))throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 @GetMapping public List<Department> all(Authentication a){guard(a);return repo.findAll();}
 @GetMapping("/{id}") public Department one(@PathVariable Long id,Authentication a){guard(a);return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @PostMapping public Department add(@RequestBody Department item,Authentication a){guard(a);item.id=null;recordValidator.validate(item,"departments");if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");item.id=null;Department saved=repo.save(item);audit.save(new AuditLog(a.getName(),"CREATE","Department",saved.id.toString()));return saved;}
 @PutMapping("/{id}") public Department update(@PathVariable Long id,@RequestBody Department item,Authentication a){guard(a);Department existing=one(id,a);item.id=id;recordValidator.validate(item,"departments");if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");existing.name=item.name;existing.status=item.status;existing.details=item.details;Department saved=repo.save(existing);audit.save(new AuditLog(a.getName(),"UPDATE","Department",id.toString()));return saved;}
 @DeleteMapping("/{id}") public void delete(@PathVariable Long id,Authentication a){guard(a);Department existing=one(id,a);existing.status="ARCHIVED";repo.save(existing);audit.save(new AuditLog(a.getName(),"ARCHIVE","Department",id.toString()));}
}