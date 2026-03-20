package com.catalog.models;

import jakarta.persistence.*;

@Entity
@Table(name = "services")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "measurement_type")
    private String measurementType;

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    private double price;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Column(name = "lab_id", nullable = false)
    private int labId;

    @Column(name = "is_active")
    private boolean isActive;

    private String standard;

    // Не хранится в БД — заполняется через JOIN в ServiceRepository
    @Transient
    private String labName;

    public Service() {}

    public Service(String name, String description, String measurementType, double price, int durationDays, int labId) {
        this.name = name;
        this.description = description;
        this.measurementType = measurementType;
        this.price = price;
        this.durationDays = durationDays;
        this.labId = labId;
        this.isActive = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMeasurementType() { return measurementType; }
    public void setMeasurementType(String measurementType) { this.measurementType = measurementType; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public int getLabId() { return labId; }
    public void setLabId(int labId) { this.labId = labId; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }
    
    public String getLabName() { return labName; }
    public void setLabName(String labName) { this.labName = labName; }
}