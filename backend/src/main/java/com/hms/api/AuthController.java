package com.hms.api;
import com.hms.repo.*;
import com.hms.model.*;
import com.hms.security.SecurityConfig;
import com.hms.validation.*;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@RestController @RequestMapping("/api/auth") public class AuthController {
 private final UserRepository users; private final RoleRepository roles; private final PasswordEncoder passwords; private final SecurityConfig security;private final AuditLogRepository audit;
 public AuthController(UserRepository u,RoleRepository r,PasswordEncoder encoder,SecurityConfig s,AuditLogRepository audit){users=u;roles=r;passwords=encoder;security=s;this.audit=audit;}
 public record Credentials(String username,String password){}
 private Map<String,Object> profile(UserAccount u){return Map.of("id",u.id,"username",u.username,"enabled",u.enabled,"roles",u.roles.stream().map(r->r.name).toList());}
 @PostMapping("/login") public Map<String,Object> login(@RequestBody Credentials c){if(c.username()==null||c.username().isBlank()||c.password()==null||c.password().isEmpty())throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Enter your username and password");UserAccount u=users.findByUsername(c.username()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid username or password"));if(!u.enabled||u.deleted||!SecurityConfig.isStaff(u)||!passwords.matches(c.password(),u.passwordHash))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid username or password");Map<String,Object> result=new LinkedHashMap<>(profile(u));result.put("token",security.token(u.username));return result;}
 @GetMapping("/me") public Map<String,Object> me(Authentication a){return profile(users.findByUsername(a.getName()).orElseThrow());}
 @GetMapping("/users") public List<Map<String,Object>> list(Authentication a){requireAdmin(a);return users.findAll().stream().filter(u->!u.deleted&&SecurityConfig.isStaff(u)).map(this::profile).toList();}
 public record NewUser(String username,String password,List<String> roles){}
 @PostMapping("/users") @Transactional public Map<String,Object> create(@RequestBody NewUser input,Authentication a){
  requireAdmin(a);AccountValidation.username(input.username());AccountValidation.password(input.password(),"password");
  if(users.findByUsername(input.username()).isPresent())throw new FormValidationException(Map.of("username","This username is already in use, including any deleted accounts. Choose another username."));
  if(input.roles()==null||input.roles().size()!=1||input.roles().get(0)==null||!SecurityConfig.STAFF_ROLES.contains(input.roles().get(0)))throw new FormValidationException(Map.of("roles","Choose one hospital staff role."));
  Role role=roles.findByName(input.roles().get(0)).orElseThrow(()->new FormValidationException(Map.of("roles","Unknown staff role.")));
  UserAccount u=new UserAccount();u.username=input.username();u.passwordHash=passwords.encode(input.password());u.roles.add(role);users.save(u);audit.save(new AuditLog(a.getName(),"CREATE_LOGIN","UserAccount",u.id.toString()));return profile(u);
 }
 public record UserUpdate(Boolean enabled,String password){}
 @PutMapping("/users/{id}") @Transactional public Map<String,Object> update(@PathVariable Long id,@RequestBody UserUpdate input,Authentication a){requireAdmin(a);UserAccount u=findStaff(id);if(Boolean.FALSE.equals(input.enabled())){if(u.username.equals(a.getName()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"You cannot disable your own account");protectLastAdmin(u);}if(input.password()!=null&&!input.password().isEmpty()){AccountValidation.password(input.password(),"password");u.passwordHash=passwords.encode(input.password());}if(input.enabled()!=null)u.enabled=input.enabled();users.save(u);audit.save(new AuditLog(a.getName(),"UPDATE_LOGIN","UserAccount",u.id.toString()));return profile(u);}
 @DeleteMapping("/users/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @Transactional public void delete(@PathVariable Long id,Authentication a){requireAdmin(a);UserAccount u=findStaff(id);if(u.username.equals(a.getName()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"You cannot delete the account you are signed in with");protectLastAdmin(u);u.enabled=false;u.deleted=true;users.save(u);audit.save(new AuditLog(a.getName(),"DELETE_LOGIN","UserAccount",u.id.toString()));}
 @PostMapping("/password") public Map<String,String> password(@RequestBody Map<String,String> body,Authentication a){UserAccount u=users.findByUsername(a.getName()).orElseThrow();String current=body.get("current"),next=body.get("next");if(current==null||!passwords.matches(current,u.passwordHash))throw new FormValidationException(Map.of("current","Current password is incorrect."));AccountValidation.password(next,"next");if(passwords.matches(next,u.passwordHash))throw new FormValidationException(Map.of("next","Choose a new password different from your current password."));u.passwordHash=passwords.encode(next);users.save(u);return Map.of("message","Password changed");}
 private UserAccount findStaff(Long id){return users.findById(id).filter(u->!u.deleted&&SecurityConfig.isStaff(u)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Staff account not found"));}
 private void protectLastAdmin(UserAccount u){if(u.enabled&&u.roles.stream().anyMatch(r->r.name.equals("ADMIN"))&&users.findAll().stream().filter(x->!x.deleted&&x.enabled&&x.roles.stream().anyMatch(r->r.name.equals("ADMIN"))).count()<=1)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"The last active administrator account cannot be disabled or deleted");}
 private void requireAdmin(Authentication a){if(a.getAuthorities().stream().noneMatch(x->x.getAuthority().equals("ROLE_ADMIN")))throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
}
