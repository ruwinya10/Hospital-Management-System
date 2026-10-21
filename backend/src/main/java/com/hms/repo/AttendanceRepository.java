package com.hms.repo;
import com.hms.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AttendanceRepository extends JpaRepository<Attendance,Long> {}