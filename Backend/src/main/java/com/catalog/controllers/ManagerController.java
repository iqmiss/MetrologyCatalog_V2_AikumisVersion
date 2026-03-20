package com.catalog.controllers;

import com.catalog.repository.OrderRepository;
import com.catalog.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Контроллер для получения статистики системы
// Используется на странице Dashboard для роли manager
@PreAuthorize("hasRole('MANAGER')")
@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "http://localhost:5173")
public class ManagerController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ManagerController(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    // GET /api/stats
    // Возвращает агрегированную статистику по заявкам и пользователям
    @GetMapping
    public ResponseEntity<?> getStats() {
        try {
            // Все агрегаты считаются одним SQL запросом на стороне БД
            Map<String, Object> stats = orderRepository.getStats();

            // Количество клиентов — отдельный быстрый COUNT запрос
            stats.put("totalClients", userRepository.countClients());

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при получении статистики"));
        }
    }
}