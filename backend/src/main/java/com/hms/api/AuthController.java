package com.hms.api;
import com.hms.repo.*;
import com.hms.model.*;
import com.hms.security.SecurityConfig;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/auth") public class AuthController {
 private final UserRepository users; private final RoleRepository roles; private final PasswordEncoder passwords; private final SecurityConfig security;
 public AuthController(UserRepository u,RoleRepository r,PasswordEncoder encoder,SecurityConfig s){users=u;roles=r;passwords=encoder;security=s;}
 public record Credentials(String username,String password){}
 private Map<String,Object> profile(UserAccount u){Map<String,Object> result=new LinkedHashMap<>();result.put("id",u.id);result.put("username",u.username);result.put("enabled",u.enabled);result.put("roles",u.roles.stream().map(r->r.name).toList());return result;}
 @PostMapping("/login") public Map<String,Object> login(@RequestBody Credentials c){if(c.username()==null||c.password()==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials");UserAccount u=users.findByUsername(c.username()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials"));if(!u.enabled||!SecurityConfig.isStaff(u)||!passwords.matches(c.password(),u.passwordHash))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials");Map<String,Object> result=profile(u);result.put("token",security.token(u.username));return result;}
 @GetMapping("/me") public Map<String,Object> me(Authentication a){return profile(users.findByUsername(a.getName()).orElseThrow());}
 @GetMapping("/users") public List<Map<String,Object>> list(Authentication a){requireAdmin(a);return users.findAll().stream().filter(SecurityConfig::isStaff).map(this::profile).toList();}
 public record NewUser(String username,String password,List<String> roles){}
 @PostMapping("/users") public Map<String,Object> create(@RequestBody NewUser input,Authentication a){
  requireAdmin(a);
  if(input.username()==null||input.username().isBlank()||input.username().length()>100||input.password()==null||input.password().length()<12||input.password().length()>72)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Username and password of 12–72 characters required");
  String username=input.username().trim();
  if(users.findByUsername(username).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Username exists");
  if(input.roles()==null||input.roles().size()!=1)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose one role");
  if(!SecurityConfig.STAFF_ROLES.contains(input.roles().get(0)))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a hospital staff role");
  Role role=roles.findByName(input.roles().get(0)).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unknown role"));
  UserAccount u=new UserAccount();u.username=username;u.passwordHash=passwords.encode(input.password());u.roles.add(role);
  return profile(users.save(u));
 }
 public record UserUpdate(Boolean enabled,String password){}
 @PutMapping("/users/{id}") public Map<String,Object> update(@PathVariable Long id,@RequestBody UserUpdate input,Authentication a){requireAdmin(a);UserAccount u=users.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));if(!SecurityConfig.isStaff(u))throw new ResponseStatusException(HttpStatus.NOT_FOUND);if(Boolean.FALSE.equals(input.enabled())&&u.username.equals(a.getName()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"You cannot disable your own account");if(input.password()!=null&&!input.password().isEmpty()){validatePassword(input.password());u.passwordHash=passwords.encode(input.password());}if(input.enabled()!=null)u.enabled=input.enabled();return profile(users.save(u));}
 @PostMapping("/password") public Map<String,String> password(@RequestBody Map<String,String> body,Authentication a){UserAccount u=users.findByUsername(a.getName()).orElseThrow();String current=body.get("current"),next=body.get("next");if(current==null||!passwords.matches(current,u.passwordHash))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Current password is incorrect");validatePassword(next);u.passwordHash=passwords.encode(next);users.save(u);return Map.of("message","Password changed");}
 private void validatePassword(String password){if(password==null||password.length()<12||password.length()>72)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Password must contain 12–72 characters");}
 private void requireAdmin(Authentication a){if(a.getAuthorities().stream().noneMatch(x->x.getAuthority().equals("ROLE_ADMIN")))throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
}
