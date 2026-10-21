package com.hms.api;
import com.hms.model.*;import com.hms.repo.*;
import org.springframework.web.bind.annotation.*;import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/leave-records") public class LeaveRecordController {
 @org.springframework.beans.factory.annotation.Autowired private com.hms.validation.RecordValidator recordValidator;
 private final LeaveRecordRepository repo;private final AuditLogRepository audit;
 public LeaveRecordController(LeaveRecordRepository r,AuditLogRepository a){repo=r;audit=a;}
 private void guard(Authentication a){if(!(a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADMIN"))))throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 @GetMapping public List<LeaveRecord> all(Authentication a){guard(a);return repo.findAll();}
 @GetMapping("/{id}") public LeaveRecord one(@PathVariable Long id,Authentication a){guard(a);return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @PostMapping public LeaveRecord add(@RequestBody LeaveRecord item,Authentication a){guard(a);item.id=null;recordValidator.validate(item,"leave-records");if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");item.id=null;LeaveRecord saved=repo.save(item);audit.save(new AuditLog(a.getName(),"CREATE","LeaveRecord",saved.id.toString()));return saved;}
 @PutMapping("/{id}") public LeaveRecord update(@PathVariable Long id,@RequestBody LeaveRecord item,Authentication a){guard(a);LeaveRecord existing=one(id,a);item.id=id;recordValidator.validate(item,"leave-records");if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");existing.name=item.name;existing.status=item.status;existing.details=item.details;LeaveRecord saved=repo.save(existing);audit.save(new AuditLog(a.getName(),"UPDATE","LeaveRecord",id.toString()));return saved;}
 @DeleteMapping("/{id}") public void delete(@PathVariable Long id,Authentication a){guard(a);LeaveRecord existing=one(id,a);existing.status="ARCHIVED";repo.save(existing);audit.save(new AuditLog(a.getName(),"ARCHIVE","LeaveRecord",id.toString()));}
}