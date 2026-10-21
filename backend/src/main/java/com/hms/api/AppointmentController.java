package com.hms.api;
import com.hms.model.*;import com.hms.repo.*;
import org.springframework.web.bind.annotation.*;import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/appointments") public class AppointmentController {
 private final AppointmentRepository repo;private final AuditLogRepository audit;
 public AppointmentController(AppointmentRepository r,AuditLogRepository a){repo=r;audit=a;}
 private void guard(Authentication a){if(!(a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADMIN")) || a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_RECEPTIONIST")) || a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_DOCTOR"))))throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 @GetMapping public List<Appointment> all(Authentication a){guard(a);return repo.findAll();}
 @GetMapping("/{id}") public Appointment one(@PathVariable Long id,Authentication a){guard(a);return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @PostMapping public Appointment add(@RequestBody Appointment item,Authentication a){guard(a);if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");item.id=null;Appointment saved=repo.save(item);audit.save(new AuditLog(a.getName(),"CREATE","Appointment",saved.id.toString()));return saved;}
 @PutMapping("/{id}") public Appointment update(@PathVariable Long id,@RequestBody Appointment item,Authentication a){guard(a);Appointment existing=one(id,a);if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");existing.name=item.name;existing.status=item.status;existing.details=item.details;Appointment saved=repo.save(existing);audit.save(new AuditLog(a.getName(),"UPDATE","Appointment",id.toString()));return saved;}
 @DeleteMapping("/{id}") public void delete(@PathVariable Long id,Authentication a){guard(a);Appointment existing=one(id,a);existing.status="ARCHIVED";repo.save(existing);audit.save(new AuditLog(a.getName(),"ARCHIVE","Appointment",id.toString()));}
}