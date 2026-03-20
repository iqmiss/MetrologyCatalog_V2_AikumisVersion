package com.catalog.models;

public class OrderItem {
    private int id;
    private int orderId;
    private String deviceType;
    private String model;
    private String serialNumber;
    private int quantity;
    private double unitPrice;

    public OrderItem() {}

    public OrderItem(int orderId, String deviceType, String model, String serialNumber, int quantity, double unitPrice) {
        this.orderId = orderId;
        this.deviceType = deviceType;
        this.model = model;
        this.serialNumber = serialNumber;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
}