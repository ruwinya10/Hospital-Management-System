package com.hms.model;
import jakarta.persistence.*;import java.time.Instant;
@Entity @Table(name="audit_logs") public class AuditLog {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public String actor;public String action;public String entityType;public String entityId;public Instant at=Instant.now();
 @Column(length=500) public String summary;
 public AuditLog(){} public AuditLog(String a,String b,String c,String d){actor=a;action=b;entityType=c;entityId=d;}
 public AuditLog(String a,String b,String c,String d,String summary){this(a,b,c,d);this.summary=summary;}
 public AuditLog(String a,String b,String c,String d,BaseRecord record){this(a,b,c,d,"Record: "+record.name+"; status: "+record.status);}
}