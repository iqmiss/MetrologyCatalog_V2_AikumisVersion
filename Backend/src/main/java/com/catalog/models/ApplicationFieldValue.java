package com.catalog.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "application_field_values")
public class ApplicationFieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "order_id", nullable = false)
    private int orderId;

    @Column(name = "field_key", nullable = false)
    private String fieldKey;

    @Column(name = "field_value", columnDefinition = "TEXT")
    private String fieldValue;

    @Column(name = "row_index")
    private int rowIndex = 0;

    @Column(name = "filled_by_role")
    private String filledByRole = "client";

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ApplicationFieldValue() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }

    public String getFieldValue() { return fieldValue; }
    public void setFieldValue(String fieldValue) { this.fieldValue = fieldValue; }

    public int getRowIndex() { return rowIndex; }
    public void setRowIndex(int rowIndex) { this.rowIndex = rowIndex; }

    public String getFilledByRole() { return filledByRole; }
    public void setFilledByRole(String filledByRole) { this.filledByRole = filledByRole; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}