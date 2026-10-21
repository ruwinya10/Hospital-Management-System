package com.hms.repo;
import com.hms.model.LeaveRecord;
import org.springframework.data.jpa.repository.JpaRepository;
public interface LeaveRecordRepository extends JpaRepository<LeaveRecord,Long> {}