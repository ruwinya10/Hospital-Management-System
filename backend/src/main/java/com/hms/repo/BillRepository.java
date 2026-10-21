package com.hms.repo;
import com.hms.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BillRepository extends JpaRepository<Bill,Long> {}