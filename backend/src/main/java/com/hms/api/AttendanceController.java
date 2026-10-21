package com.hms.api;
import com.hms.model.*;import com.hms.repo.*;
import org.springframework.web.bind.annotation.*;import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/attendance") public class AttendanceController {
 private final AttendanceRepository repo;private final AuditLogRepository audit;
 public AttendanceController(AttendanceRepository r,AuditLogRepository a){repo=r;audit=a;}
 private void guard(Authentication a){if(!(a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADMIN"))))throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 @GetMapping public List<Attendance> all(Authentication a){guard(a);return repo.findAll();}
 @GetMapping("/{id}") public Attendance one(@PathVariable Long id,Authentication a){guard(a);return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @PostMapping public Attendance add(@RequestBody Attendance item,Authentication a){guard(a);if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");item.id=null;Attendance saved=repo.save(item);audit.save(new AuditLog(a.getName(),"CREATE","Attendance",saved.id.toString()));return saved;}
 @PutMapping("/{id}") public Attendance update(@PathVariable Long id,@RequestBody Attendance item,Authentication a){guard(a);Attendance existing=one(id,a);if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");existing.name=item.name;existing.status=item.status;existing.details=item.details;Attendance saved=repo.save(existing);audit.save(new AuditLog(a.getName(),"UPDATE","Attendance",id.toString()));return saved;}
 @DeleteMapping("/{id}") public void delete(@PathVariable Long id,Authentication a){guard(a);Attendance existing=one(id,a);existing.status="ARCHIVED";repo.save(existing);audit.save(new AuditLog(a.getName(),"ARCHIVE","Attendance",id.toString()));}
}