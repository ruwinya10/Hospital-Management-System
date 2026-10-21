package com.hms.model;
import jakarta.persistence.*;
@Entity public class Role { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @Column(unique=true,nullable=false) public String name; public Role(){} public Role(String n){name=n;} }