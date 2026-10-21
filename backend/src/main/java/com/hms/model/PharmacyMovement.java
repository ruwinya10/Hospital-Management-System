package com.hms.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity @Table(name="pharmacy_movements",indexes={@Index(columnList="medicineId"),@Index(columnList="occurredAt")})
public class PharmacyMovement {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public Long medicineId;
 @Column(nullable=false,length=20) public String type;
 @Column(nullable=false,precision=18,scale=3) public BigDecimal quantity;
 @Column(nullable=false,precision=18,scale=2) public BigDecimal unitPrice;
 @Column(nullable=false) public Instant occurredAt;
 public Long patientId;
 @Column(length=200) public String reference;
 @Column(nullable=false) public String recordedBy;
}
