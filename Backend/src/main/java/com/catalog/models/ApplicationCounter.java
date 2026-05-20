package com.catalog.models;

import jakarta.persistence.*;

@Entity
@Table(name = "application_counters")
public class ApplicationCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "service_code", nullable = false)
    private String serviceCode;

    @Column(name = "subservice_code", nullable = false)
    private String subserviceCode;

    @Column(name = "last_number", nullable = false)
    private int lastNumber = 0;

    public ApplicationCounter() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getServiceCode() { return serviceCode; }
    public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }

    public String getSubserviceCode() { return subserviceCode; }
    public void setSubserviceCode(String subserviceCode) { this.subserviceCode = subserviceCode; }

    public int getLastNumber() { return lastNumber; }
    public void setLastNumber(int lastNumber) { this.lastNumber = lastNumber; }
}