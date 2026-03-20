package com.catalog.repository;

import com.catalog.models.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Integer> {
    Optional<Result> findByOrderId(int orderId);
    List<Result> findByMetrologistId(int metrologistId);
}