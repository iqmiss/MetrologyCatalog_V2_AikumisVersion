package com.catalog.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "order_id", nullable = false)
    private int orderId;

    @Column(name = "sender_id", nullable = false)
    private int senderId;

    @Column(name = "sender_role", nullable = false)
    private String senderRole;

    @Column(name = "message_text", columnDefinition = "TEXT")
    private String messageText;

    @Column(name = "attachment_base64", columnDefinition = "MEDIUMTEXT")
    private String attachmentBase64;

    @Column(name = "attachment_name")
    private String attachmentName;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "is_read")
    private boolean isRead = false;

    public ChatMessage() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public String getAttachmentBase64() { return attachmentBase64; }
    public void setAttachmentBase64(String attachmentBase64) { this.attachmentBase64 = attachmentBase64; }

    public String getAttachmentName() { return attachmentName; }
    public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}