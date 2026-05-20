package com.catalog.controllers;

import com.catalog.models.ChatMessage;
import com.catalog.repository.ChatMessageRepository;
import com.catalog.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final JwtUtil jwtUtil;

    public ChatController(ChatMessageRepository chatMessageRepository,
                          JwtUtil jwtUtil) {
        this.chatMessageRepository = chatMessageRepository;
        this.jwtUtil = jwtUtil;
    }

    // GET /api/chat/{orderId}
    // Returns all messages for an order
    @GetMapping("/{orderId}")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable int orderId) {
        return ResponseEntity.ok(
            chatMessageRepository.findByOrderIdOrderBySentAt(orderId)
        );
    }

    // POST /api/chat/{orderId}
    // Send a message
    @PostMapping("/{orderId}")
    public ResponseEntity<ChatMessage> sendMessage(
            @PathVariable int orderId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {

        String token = authHeader.replace("Bearer ", "");
        int senderId = jwtUtil.getUserId(token);
        String senderRole = jwtUtil.getRole(token);

        ChatMessage message = new ChatMessage();
        message.setOrderId(orderId);
        message.setSenderId(senderId);
        message.setSenderRole(senderRole);
        message.setMessageText(body.get("messageText"));
        message.setAttachmentBase64(body.get("attachmentBase64"));
        message.setAttachmentName(body.get("attachmentName"));
        message.setSentAt(LocalDateTime.now());
        message.setRead(false);

        ChatMessage saved = chatMessageRepository.save(message);
        return ResponseEntity.status(201).body(saved);
    }

    // PUT /api/chat/{orderId}/read
    // Mark all messages in an order as read for the requesting user
    @PutMapping("/{orderId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable int orderId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        int userId = jwtUtil.getUserId(token);

        List<ChatMessage> unread = chatMessageRepository
                .findByOrderIdAndIsRead(orderId, false);

        unread.stream()
              .filter(m -> m.getSenderId() != userId)
              .forEach(m -> m.setRead(true));

        chatMessageRepository.saveAll(unread);
        return ResponseEntity.ok().build();
    }
}