package com.catalog.models;

import jakarta.persistence.*;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "company_id", nullable = false)
    private int companyId;

    @Column(nullable = false)
    private String type;

    private String model;

    @Column(name = "serial_number", nullable = false, unique = true)
    private String serialNumber;

    @Column(name = "last_verified_at")
    private java.time.LocalDateTime lastVerifiedAt;

    @Column(name = "next_verification_date")
    private java.time.LocalDate nextVerificationDate;

    public Device() {}

    public Device(int companyId, String type, String model, String serialNumber) {
        this.companyId = companyId;
        this.type = type;
        this.model = model;
        this.serialNumber = serialNumber;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getCompanyId() { return companyId; }
    public void setCompanyId(int companyId) { this.companyId = companyId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public java.time.LocalDateTime getLastVerifiedAt() { return lastVerifiedAt; }
    public void setLastVerifiedAt(java.time.LocalDateTime lastVerifiedAt) { this.lastVerifiedAt = lastVerifiedAt; }

    public java.time.LocalDate getNextVerificationDate() { return nextVerificationDate; }
    public void setNextVerificationDate(java.time.LocalDate nextVerificationDate) { this.nextVerificationDate = nextVerificationDate; }
}