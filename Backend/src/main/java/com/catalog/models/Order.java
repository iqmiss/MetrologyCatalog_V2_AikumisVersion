package com.catalog.models;

public class Order {
    private int id;
    private String orderNumber;
    private int clientId;
    private int serviceId;
    private int labId;
    private String status;
    private double totalPrice;
    private java.time.LocalDate dueDate;
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