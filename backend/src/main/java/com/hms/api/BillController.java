package com.hms.api;
import com.hms.model.*;import com.hms.repo.*;
import org.springframework.web.bind.annotation.*;import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/bills") public class BillController {
 private final BillRepository repo;private final AuditLogRepository audit;
 public BillController(BillRepository r,AuditLogRepository a){repo=r;audit=a;}
 private void guard(Authentication a){if(!(a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADMIN")) || a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ACCOUNTANT")) || a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_RECEPTIONIST"))))throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 @GetMapping public List<Bill> all(Authentication a){guard(a);return repo.findAll();}
 @GetMapping("/{id}") public Bill one(@PathVariable Long id,Authentication a){guard(a);return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @PostMapping public Bill add(@RequestBody Bill item,Authentication a){guard(a);if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");item.id=null;Bill saved=repo.save(item);audit.save(new AuditLog(a.getName(),"CREATE","Bill",saved.id.toString()));return saved;}
 @PutMapping("/{id}") public Bill update(@PathVariable Long id,@RequestBody Bill item,Authentication a){guard(a);Bill existing=one(id,a);if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");existing.name=item.name;existing.status=item.status;existing.details=item.details;Bill saved=repo.save(existing);audit.save(new AuditLog(a.getName(),"UPDATE","Bill",id.toString()));return saved;}
 @DeleteMapping("/{id}") public void delete(@PathVariable Long id,Authentication a){guard(a);Bill existing=one(id,a);existing.status="ARCHIVED";repo.save(existing);audit.save(new AuditLog(a.getName(),"ARCHIVE","Bill",id.toString()));}
}