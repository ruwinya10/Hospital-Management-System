package com.hms.repo;
import com.hms.model.PharmacyItem;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PharmacyItemRepository extends JpaRepository<PharmacyItem,Long> {}