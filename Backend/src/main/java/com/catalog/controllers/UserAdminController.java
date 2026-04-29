package com.catalog.controllers;

import com.catalog.models.User;
import com.catalog.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Контроллер для административных операций с пользователями
// Используется менеджером для получения списка клиентов
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserAdminController {

    private final UserRepository userRepository;

    public UserAdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // GET /api/users/clients
    // Возвращает список всех клиентов — используется менеджером при создании заявки
    @GetMapping("/clients")
    public ResponseEntity<?> getClients() {
        try {
            List<User> clients = userRepository.findByRole("client");
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Ошибка при получении клиентов");
            return ResponseEntity.status(500).body(error);
        }
    }
}