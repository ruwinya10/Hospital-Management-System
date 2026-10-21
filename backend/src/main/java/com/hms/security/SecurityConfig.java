package com.hms.security;
import com.hms.repo.UserRepository;
import io.jsonwebtoken.Jwts;import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.nio.charset.StandardCharsets;import java.util.*;
@Configuration public class SecurityConfig {
 public static final Set<String> STAFF_ROLES=Set.of("ADMIN","DOCTOR","NURSE","RECEPTIONIST","LAB","PHARMACIST","ACCOUNTANT");
 public static boolean isStaff(com.hms.model.UserAccount user){return user.roles.stream().anyMatch(r->STAFF_ROLES.contains(r.name));}
 @Value("${hms.jwt.secret}") String secret;
 @Value("${hms.frontend.origin}") String origin;
 private final UserRepository users;
 public SecurityConfig(UserRepository users){this.users=users;}
 @Bean PasswordEncoder encoder(){return new BCryptPasswordEncoder();}
 @Bean CorsConfigurationSource cors(){CorsConfiguration c=new CorsConfiguration();c.setAllowedOrigins(List.of(origin));c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("Authorization","Content-Type"));UrlBasedCorsConfigurationSource s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;}
 @Bean SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http)throws Exception {
  http.csrf(c->c.disable()).cors(c->{}).sessionManagement(s->s.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
  .authorizeHttpRequests(a->a.dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll().requestMatchers("/api/auth/login","/actuator/health").permitAll().requestMatchers(HttpMethod.OPTIONS,"/**").permitAll().anyRequest().authenticated())
  .addFilterBefore(new org.springframework.web.filter.OncePerRequestFilter(){@Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws java.io.IOException,ServletException{
   String h=req.getHeader("Authorization");if(h!=null&&h.startsWith("Bearer ")){try{String username=Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).build().parseSignedClaims(h.substring(7)).getPayload().getSubject();users.findByUsername(username).filter(u->u.enabled&&!u.deleted&&isStaff(u)).ifPresent(u->{List<SimpleGrantedAuthority> roles=u.roles.stream().map(r->new SimpleGrantedAuthority("ROLE_"+r.name)).toList();SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(username,null,roles));});}catch(Exception ignored){}}
   chain.doFilter(req,res);
  }},UsernamePasswordAuthenticationFilter.class);
 return http.build();}
 public String token(String username){if(secret.length()<32)throw new IllegalStateException("JWT_SECRET must be at least 32 characters");return Jwts.builder().subject(username).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+30*60*1000)).signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).compact();}
}


