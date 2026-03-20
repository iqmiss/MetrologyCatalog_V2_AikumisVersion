package com.catalog.models;

import jakarta.persistence.*;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "order_id")
    private Integer orderId;

    @Column(nullable = false)
    private String message;

    @Column(name = "notification_type", columnDefinition = "ENUM('order_status','document_ready','reminder')")
    private String notificationType;

    @Column(name = "is_read")
    private boolean isRead;

    @Column(name = "read_at")
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