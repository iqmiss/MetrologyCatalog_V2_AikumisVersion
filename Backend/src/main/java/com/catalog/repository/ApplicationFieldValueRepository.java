package com.catalog.repository;

import com.catalog.models.ApplicationFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface ApplicationFieldValueRepository extends JpaRepository<ApplicationFieldValue, Integer> {
    List<ApplicationFieldValue> findByOrderId(int orderId);
    List<ApplicationFieldValue> findByOrderIdAndFilledByRole(int orderId, String filledByRole);
    List<ApplicationFieldValue> findByOrderIdAndRowIndex(int orderId, int rowIndex);

    @Transactional
    void deleteByOrderId(int orderId);
}