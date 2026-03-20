package com.catalog.models;

public class Notification {
    private int id;
    private int userId;
    private Integer orderId;
    private String message;
    private String notificationType;
    private boolean isRead;
    private java.time.LocalDateTime readAt;

    public Notification() {}

    public Notification(int userId, String message, String notificationType) {
        this.userId = userId;
        this.message = message;
        this.notificationType = notificationType;
        this.isRead = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public java.time.LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(java.time.LocalDateTime readAt) { this.readAt = readAt; }
}