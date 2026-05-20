package com.catalog.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_demands")
public class OrderDemand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "order_id", nullable = false)
    private int orderId;

    @Column(name = "created_by", nullable = false)
    private int createdBy;

    @Column(name = "demand_text", nullable = false, columnDefinition = "TEXT")
    private String demandText;

    @Column(name = "status", nullable = false,
            columnDefinition = "ENUM('open','fulfilled')")
    private String status = "open";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "fulfilled_at")
    private LocalDateTime fulfilledAt;

    public OrderDemand() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public String getDemandText() { return demandText; }
    public void setDemandText(String demandText) { this.demandText = demandText; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getFulfilledAt() { return fulfilledAt; }
    public void setFulfilledAt(LocalDateTime fulfilledAt) { this.fulfilledAt = fulfilledAt; }
}