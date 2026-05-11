package com.catalog.repository;

import com.catalog.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByClientId(int clientId);
    List<Order> findByLabId(int labId);
    List<Order> findByStatus(String status);

    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(o.price), 0) FROM Order o WHERE o.status = 'completed'")
    double sumRevenue();
}