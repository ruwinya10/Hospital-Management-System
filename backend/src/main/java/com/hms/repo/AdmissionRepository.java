package com.hms.repo;
import com.hms.model.Admission;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AdmissionRepository extends JpaRepository<Admission,Long> {}