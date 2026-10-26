package com.hms.api;
import com.hms.model.*;import com.hms.repo.*;
import org.springframework.web.bind.annotation.*;import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/employees") public class EmployeeController {
 @org.springframework.beans.factory.annotation.Autowired private com.hms.validation.RecordValidator recordValidator;
 private final EmployeeRepository repo;private final AuditLogRepository audit;
 public EmployeeController(EmployeeRepository r,AuditLogRepository a){repo=r;audit=a;}
 private void guard(Authentication a){if(!(a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADMIN"))))throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 @GetMapping public List<Employee> all(Authentication a){guard(a);return repo.findAll();}
 @GetMapping("/{id}") public Employee one(@PathVariable Long id,Authentication a){guard(a);return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @PostMapping public Employee add(@RequestBody Employee item,Authentication a){guard(a);item.id=null;recordValidator.validate(item,"employees");if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");item.id=null;Employee saved=repo.save(item);audit.save(new AuditLog(a.getName(),"CREATE","Employee",saved.id.toString()));return saved;}
 @PutMapping("/{id}") public Employee update(@PathVariable Long id,@RequestBody Employee item,Authentication a){guard(a);Employee existing=one(id,a);item.id=id;recordValidator.validate(item,"employees");if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");existing.name=item.name;existing.status=item.status;existing.details=item.details;Employee saved=repo.save(existing);audit.save(new AuditLog(a.getName(),"UPDATE","Employee",id.toString()));return saved;}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @org.springframework.transaction.annotation.Transactional public void delete(@PathVariable Long id,Authentication a){guard(a);Employee existing=one(id,a);repo.delete(existing);audit.save(new AuditLog(a.getName(),"DELETE","Employee",id.toString()));}
}