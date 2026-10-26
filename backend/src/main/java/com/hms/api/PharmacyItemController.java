package com.hms.api;
import com.hms.model.*;import com.hms.repo.*;
import com.fasterxml.jackson.databind.*;
import jakarta.persistence.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;import org.springframework.web.server.ResponseStatusException;
import java.util.*;import java.math.BigDecimal;
@RestController @RequestMapping("/api/pharmacy") public class PharmacyItemController {
 @org.springframework.beans.factory.annotation.Autowired private com.hms.validation.RecordValidator recordValidator;
 private final PharmacyItemRepository repo;private final AuditLogRepository audit;private final EntityManager em;private final ObjectMapper json;
 public PharmacyItemController(PharmacyItemRepository r,AuditLogRepository a,EntityManager e,ObjectMapper j){repo=r;audit=a;em=e;json=j;}
 private void guard(Authentication a){if(a.getAuthorities().stream().noneMatch(x->List.of("ROLE_ADMIN","ROLE_PHARMACIST").contains(x.getAuthority())))throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 private JsonNode validate(PharmacyItem item){if(item.name==null||item.name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");try{JsonNode d=json.readTree(item.details);if(d==null||!d.isObject())throw new IllegalArgumentException();for(String key:List.of("quantity","unitPrice","reorderLevel")){String text=d.path(key).asText("");BigDecimal number=text.isBlank()?BigDecimal.ZERO:new BigDecimal(text);if(number.signum()<0||number.compareTo(new BigDecimal("999999999"))>0||number.scale()>(key.equals("unitPrice")?2:3))throw new IllegalArgumentException();}return d;}catch(Exception e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Medicine quantity, price and reorder level must be valid non-negative numbers (price: 2 decimals; stock: 3 decimals)");}}
 private BigDecimal quantity(JsonNode details){String text=details.path("quantity").asText("");return text.isBlank()?BigDecimal.ZERO:new BigDecimal(text);}
 @GetMapping public List<PharmacyItem> all(Authentication a){guard(a);return repo.findAll();}
 @GetMapping("/{id}") public PharmacyItem one(@PathVariable Long id,Authentication a){guard(a);return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @PostMapping public PharmacyItem add(@RequestBody PharmacyItem item,Authentication a){guard(a);item.id=null;recordValidator.validate(item,"pharmacy");validate(item);item.id=null;item.createdAt=java.time.Instant.now();PharmacyItem saved=repo.save(item);audit.save(new AuditLog(a.getName(),"CREATE","PharmacyItem",saved.id.toString()));return saved;}
 @PutMapping("/{id}") @Transactional public PharmacyItem update(@PathVariable Long id,@RequestBody PharmacyItem item,Authentication a){guard(a);item.id=id;recordValidator.validate(item,"pharmacy");JsonNode d=validate(item);PharmacyItem existing=em.find(PharmacyItem.class,id,LockModeType.PESSIMISTIC_WRITE);if(existing==null)throw new ResponseStatusException(HttpStatus.NOT_FOUND);try{if(quantity(d).compareTo(quantity(json.readTree(existing.details)))!=0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Use Reports > Pharmacy > Record stock movement to change quantity");}catch(com.fasterxml.jackson.core.JsonProcessingException e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Existing medicine details are invalid");}existing.name=item.name;existing.status=item.status;existing.details=item.details;audit.save(new AuditLog(a.getName(),"UPDATE","PharmacyItem",id.toString()));return existing;}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @Transactional public void delete(@PathVariable Long id,Authentication a){guard(a);PharmacyItem existing=em.find(PharmacyItem.class,id,LockModeType.PESSIMISTIC_WRITE);if(existing==null)throw new ResponseStatusException(HttpStatus.NOT_FOUND);em.remove(existing);audit.save(new AuditLog(a.getName(),"DELETE","PharmacyItem",id.toString()));}
}
