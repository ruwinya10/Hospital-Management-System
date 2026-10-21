package com.hms.model;
import jakarta.persistence.*;import java.time.Instant;
@Entity @Table(name="audit_logs") public class AuditLog {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public String actor;public String action;public String entityType;public String entityId;public Instant at=Instant.now();
 public AuditLog(){} public AuditLog(String a,String b,String c,String d){actor=a;action=b;entityType=c;entityId=d;}
}