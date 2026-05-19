package com.catalog.repository;

import com.catalog.models.Subservice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubserviceRepository extends JpaRepository<Subservice, Integer> {
    List<Subservice> findByServiceId(int serviceId);
    List<Subservice> findByServiceIdAndIsActive(int serviceId, boolean isActive);
}