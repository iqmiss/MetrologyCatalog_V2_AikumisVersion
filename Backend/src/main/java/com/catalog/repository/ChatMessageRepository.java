package com.catalog.repository;

import com.catalog.models.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    List<ChatMessage> findByOrderIdOrderBySentAt(int orderId);
    List<ChatMessage> findByOrderIdAndIsRead(int orderId, boolean isRead);
    int countByOrderIdAndIsReadAndSenderIdNot(int orderId, boolean isRead, int senderId);
}