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
 public String getRecordId(){
  String prefix=switch(getClass().getSimpleName()){case "Patient"->"P";case "Doctor"->"D";case "Department"->"DEP";case "Appointment"->"APT";case "Admission"->"ADM";case "MedicalRecord"->"MR";case "LaboratoryTest"->"LAB";case "PharmacyItem"->"MED";case "Bill"->"B";case "Payment"->"PAY";case "Employee"->"E";case "Attendance"->"ATT";case "LeaveRecord"->"LV";case "Document"->"DOC";default->"R";};
  return id==null?null:prefix+String.format(java.util.Locale.ROOT,"%04d",id);
 }
 @PreUpdate void touch(){updatedAt=Instant.now();}
}