package com.catalog.models;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "client_id", nullable = false)
    private int clientId;

    @Column(name = "service_id", nullable = false)
    private int serviceId;

    @Column(name = "lab_id", nullable = false)
    private int labId;

    @Column(columnDefinition = "ENUM('new','awaiting_payment','awaiting_delivery','received_in_lab','in_work','under_review','completed', 'cancelled')")
    private String status;

    @Column(name = "total_price", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private double totalPrice;

    @Column(name = "due_date")
    private java.time.LocalDate dueDate;

    @Column(name = "metrologist_id")
    private Integer metrologistId;

    public Order() {}

    public Order(String orderNumber, int clientId, int serviceId, int labId, double totalPrice) {
        this.orderNumber = orderNumber;
        this.clientId = clientId;
        this.serviceId = serviceId;
        this.labId = labId;
        this.totalPrice = totalPrice;
        this.status = "new";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public int getLabId() { return labId; }
    public void setLabId(int labId) { this.labId = labId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public java.time.LocalDate getDueDate() { return dueDate; }
    public void setDueDate(java.time.LocalDate dueDate) { this.dueDate = dueDate; }

    public Integer getMetrologistId() { return metrologistId; }
    public void setMetrologistId(Integer metrologistId) { this.metrologistId = metrologistId; }
}