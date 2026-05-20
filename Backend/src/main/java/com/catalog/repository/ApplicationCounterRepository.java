package com.catalog.repository;

import com.catalog.models.ApplicationCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface ApplicationCounterRepository extends JpaRepository<ApplicationCounter, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ApplicationCounter c WHERE c.serviceCode = :serviceCode AND c.subserviceCode = :subserviceCode")
    Optional<ApplicationCounter> findByCodesForUpdate(
        @Param("serviceCode") String serviceCode,
        @Param("subserviceCode") String subserviceCode
    );
}