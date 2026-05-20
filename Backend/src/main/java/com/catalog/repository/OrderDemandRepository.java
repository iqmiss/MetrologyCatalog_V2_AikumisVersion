package com.catalog.repository;

import com.catalog.models.OrderDemand;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderDemandRepository extends JpaRepository<OrderDemand, Integer> {
    List<OrderDemand> findByOrderIdOrderByCreatedAt(int orderId);
    List<OrderDemand> findByOrderIdAndStatus(int orderId, String status);
    int countByOrderIdAndStatus(int orderId, String status);
}