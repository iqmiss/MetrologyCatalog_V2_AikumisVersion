package com.catalog.repository;

import com.catalog.models.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Integer> {
    List<Service> findByMeasurementTypeAndIsActiveTrue(String measurementType);
    List<Service> findByLabIdAndIsActiveTrue(int labId);
    List<Service> findByIsActiveTrue();
}