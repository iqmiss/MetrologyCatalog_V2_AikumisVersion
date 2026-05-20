package com.catalog.repository;

import com.catalog.models.SubserviceField;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubserviceFieldRepository extends JpaRepository<SubserviceField, Integer> {
    List<SubserviceField> findBySubserviceIdOrderBySortOrder(int subserviceId);
    List<SubserviceField> findBySubserviceIdAndIsRepeating(int subserviceId, boolean isRepeating);
}