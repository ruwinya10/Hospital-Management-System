package com.hms.model;
import jakarta.persistence.*;import java.util.*;
@Entity @Table(name="users") public class UserAccount {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(unique=true,nullable=false) public String username;
 @Column(nullable=false) public String passwordHash;
 public boolean enabled=true;
 @ManyToMany(fetch=FetchType.EAGER) @JoinTable(name="user_roles",joinColumns=@JoinColumn(name="user_id"),inverseJoinColumns=@JoinColumn(name="role_id")) public Set<Role> roles=new HashSet<>();
}
