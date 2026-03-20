package com.catalog.controllers;

import com.catalog.repository.OrderRepository;
import com.catalog.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// Контроллер для получения статистики системы
// Используется на странице Dashboard для роли manager
@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "http://localhost:5173")
public class StatsController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public StatsController(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    // GET /api/stats
    // Возвращает агрегированную статистику по заявкам и пользователям
    @GetMapping
    public ResponseEntity<?> getStats() {
        try {
            // Загружаем все заявки и пользователей из БД
            var orders = orderRepository.findAll();
            var users = userRepository.findAll();

            // Подсчёт заявок по статусам с помощью Stream API
            long total = orders.size();
            long completed = orders.stream().filter(o -> "completed".equals(o.getStatus())).count();
            long inWork = orders.stream().filter(o -> "in_work".equals(o.getStatus())).count();
            long newOrders = orders.stream().filter(o -> "new".equals(o.getStatus())).count();
            long awaitingPayment = orders.stream().filter(o -> "awaiting_payment".equals(o.getStatus())).count();

            // Суммарная выручка только по завершённым заявкам
            double totalRevenue = orders.stream()
                .filter(o -> "completed".equals(o.getStatus()))
                .mapToDouble(o -> o.getTotalPrice())
                .sum();

            // Количество пользователей с ролью client
            long totalClients = users.stream().filter(u -> "client".equals(u.getRole())).count();

            // Формируем ответ в виде Map который Spring сериализует в JSON
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalOrders", total);
            stats.put("completedOrders", completed);
            stats.put("inWorkOrders", inWork);
            stats.put("newOrders", newOrders);
            stats.put("awaitingPayment", awaitingPayment);
            stats.put("totalRevenue", totalRevenue);
            stats.put("totalClients", totalClients);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при получении статистики"));
        }
    }
}