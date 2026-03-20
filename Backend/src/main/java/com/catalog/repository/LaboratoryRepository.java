package com.catalog.repository;

import com.catalog.models.Laboratory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaboratoryRepository extends JpaRepository<Laboratory, Integer> {
    List<Laboratory> findByCity(String city);
}