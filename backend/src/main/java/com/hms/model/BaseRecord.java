package com.hms.model;
import jakarta.persistence.*;
import java.time.Instant;
@MappedSuperclass public abstract class BaseRecord {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public String name;
 public String status="ACTIVE";
 @Lob @Column(columnDefinition="LONGTEXT") public String details="{}";
 public Instant createdAt=Instant.now();
 public Instant updatedAt=Instant.now();
 @PreUpdate void touch(){updatedAt=Instant.now();}
}