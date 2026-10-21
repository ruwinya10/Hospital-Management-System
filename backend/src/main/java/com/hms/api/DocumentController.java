package com.hms.api;
import com.hms.model.*;import com.hms.repo.*;
import org.springframework.web.bind.annotation.*;import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/documents") public class DocumentController {
 @org.springframework.beans.factory.annotation.Autowired private com.hms.validation.RecordValidator recordValidator;
 private final DocumentRepository repo;private final AuditLogRepository audit;
 public DocumentController(DocumentRepository r,AuditLogRepository a){repo=r;audit=a;}
 private void guard(Authentication a){if(!(a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADMIN")) || a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_DOCTOR")) || a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_NURSE")) || a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_RECEPTIONIST"))))throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 @GetMapping public List<Document> all(Authentication a){guard(a);return repo.findAll();}
 @GetMapping("/{id}") public Document one(@PathVariable Long id,Authentication a){guard(a);return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @PostMapping public Document add(@RequestBody Document item,Authentication a){guard(a);item.id=null;recordValidator.validate(item,"documents");if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");item.id=null;Document saved=repo.save(item);audit.save(new AuditLog(a.getName(),"CREATE","Document",saved.id.toString()));return saved;}
 @PutMapping("/{id}") public Document update(@PathVariable Long id,@RequestBody Document item,Authentication a){guard(a);Document existing=one(id,a);item.id=id;recordValidator.validate(item,"documents");if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");existing.name=item.name;existing.status=item.status;existing.details=item.details;Document saved=repo.save(existing);audit.save(new AuditLog(a.getName(),"UPDATE","Document",id.toString()));return saved;}
 @DeleteMapping("/{id}") public void delete(@PathVariable Long id,Authentication a){guard(a);Document existing=one(id,a);existing.status="ARCHIVED";repo.save(existing);audit.save(new AuditLog(a.getName(),"ARCHIVE","Document",id.toString()));}
}