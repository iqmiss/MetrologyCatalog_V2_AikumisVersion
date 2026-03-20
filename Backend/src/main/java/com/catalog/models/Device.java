package com.catalog.models;

public class Device {
    private int id;
    private int companyId;
    private String type;
    private String model;
    private String serialNumber;
    private java.time.LocalDateTime lastVerifiedAt;
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