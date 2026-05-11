package com.catalog.controllers;

import com.catalog.models.Notification;
import com.catalog.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // GET /api/notifications?userId=1
    // Все уведомления пользователя (последние первые)
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam int userId) {
        try {
            List<Notification> notifications =
                notificationRepository.findByUserIdOrderByIdDesc(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при получении уведомлений"));
        }
    }

    // GET /api/notifications/unread?userId=1
    // Только непрочитанные уведомления
    @GetMapping("/unread")
    public ResponseEntity<?> getUnread(@RequestParam int userId) {
        try {
            List<Notification> notifications =
                notificationRepository.findByUserIdAndIsReadFalseOrderByIdDesc(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при получении уведомлений"));
        }
    }

    // PUT /api/notifications/{id}/read
    // Отметить уведомление как прочитанное
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable int id) {
        try {
            Notification notification = notificationRepository.findById(id).orElse(null);
            if (notification == null)
                return ResponseEntity.status(404).body(Map.of("message", "Уведомление не найдено"));

            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при обновлении уведомления"));
        }
    }

    // PUT /api/notifications/read-all?userId=1
    // Отметить все уведомления пользователя как прочитанные
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@RequestParam int userId) {
        try {
            List<Notification> unread =
                notificationRepository.findByUserIdAndIsReadFalseOrderByIdDesc(userId);
            unread.forEach(n -> {
                n.setRead(true);
                n.setReadAt(LocalDateTime.now());
            });
            notificationRepository.saveAll(unread);
            return ResponseEntity.ok(Map.of("updated", unread.size()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при обновлении уведомлений"));
        }
    }
}