package com.hms.config;
import com.hms.model.*;import com.hms.repo.*;import org.springframework.boot.*;import org.springframework.context.annotation.*;import org.springframework.security.crypto.password.PasswordEncoder;import org.springframework.beans.factory.annotation.Value;
@Configuration public class SeedConfig {
 @Bean CommandLineRunner seed(RoleRepository roles,UserRepository users,PasswordEncoder encoder,@Value("${HMS_ADMIN_PASSWORD:}") String initialPassword){return args->{for(String r:new String[]{"ADMIN","DOCTOR","NURSE","RECEPTIONIST","LAB","PHARMACIST","ACCOUNTANT"})roles.findByName(r).orElseGet(()->roles.save(new com.hms.model.Role(r)));if(users.count()==0){if(initialPassword.length()<12)throw new IllegalStateException("Set HMS_ADMIN_PASSWORD to 12+ characters before first startup");UserAccount u=new UserAccount();u.username="admin";u.passwordHash=encoder.encode(initialPassword);u.roles.add(roles.findByName("ADMIN").orElseThrow());users.save(u);}};}
}

