package com.hms.repo;
import com.hms.model.AuditLog;import org.springframework.data.jpa.repository.JpaRepository;
public interface AuditLogRepository extends JpaRepository<AuditLog,Long>{}