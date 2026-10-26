package com.hms.api;
import com.hms.model.*;import com.hms.repo.*;
import org.springframework.web.bind.annotation.*;import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/payments") public class PaymentController {
 @org.springframework.beans.factory.annotation.Autowired private com.hms.validation.RecordValidator recordValidator;
 private final PaymentRepository repo;private final AuditLogRepository audit;
 public PaymentController(PaymentRepository r,AuditLogRepository a){repo=r;audit=a;}
 private void guard(Authentication a){if(!(a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADMIN")) || a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ACCOUNTANT"))))throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 @GetMapping public List<Payment> all(Authentication a){guard(a);return repo.findAll();}
 @GetMapping("/{id}") public Payment one(@PathVariable Long id,Authentication a){guard(a);return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @PostMapping public Payment add(@RequestBody Payment item,Authentication a){guard(a);item.id=null;recordValidator.validate(item,"payments");if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");item.id=null;Payment saved=repo.save(item);audit.save(new AuditLog(a.getName(),"CREATE","Payment",saved.id.toString()));return saved;}
 @PutMapping("/{id}") public Payment update(@PathVariable Long id,@RequestBody Payment item,Authentication a){guard(a);Payment existing=one(id,a);item.id=id;recordValidator.validate(item,"payments");if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");existing.name=item.name;existing.status=item.status;existing.details=item.details;Payment saved=repo.save(existing);audit.save(new AuditLog(a.getName(),"UPDATE","Payment",id.toString()));return saved;}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @org.springframework.transaction.annotation.Transactional public void delete(@PathVariable Long id,Authentication a){guard(a);Payment existing=one(id,a);repo.delete(existing);audit.save(new AuditLog(a.getName(),"DELETE","Payment",id.toString()));}
}