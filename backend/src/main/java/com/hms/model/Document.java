package com.hms.model;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity @Table(name="documents") public class Document extends BaseRecord {
 @JsonIgnore @Lob @Column(columnDefinition="LONGBLOB") public byte[] content;
 public String contentType;
 public Long fileSize;
}
