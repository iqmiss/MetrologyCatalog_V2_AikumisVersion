package com.catalog.controllers;

import com.catalog.models.User;
import com.catalog.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Контроллер для административного управления пользователями
// Доступен только для роли admin — используется на странице AdminUsers
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    // Репозиторий для работы с таблицей users в БД
    private final UserRepository userRepository;

    // Spring автоматически передаёт зависимость через конструктор (Dependency Injection)
    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // GET /api/users
    // Возвращает список всех пользователей системы
    // Используется на странице AdminUsers для отображения таблицы
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при получении пользователей"));
        }
    }

    // PUT /api/users/{id}/role
    // Меняет роль пользователя (client, metrolog, manager, admin)
    // Администратор может повышать или понижать права через выпадающий список
    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateRole(
            @PathVariable int id,
            @RequestBody UpdateRoleRequest request
    ) {
        try {
            User user = userRepository.findById(id);
            if (user == null) {
                return ResponseEntity.status(404).body(errorResponse("Пользователь не найден"));
            }

            // Обновляем роль и сохраняем в БД
            user.setRole(request.getRole());
            userRepository.update(user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при смене роли"));
        }
    }

    // PUT /api/users/{id}/active
    // Активирует или блокирует пользователя
    // Заблокированный пользователь не может войти в систему
    @PutMapping("/{id}/active")
    public ResponseEntity<?> updateActive(
            @PathVariable int id,
            @RequestBody UpdateActiveRequest request
    ) {
        try {
            User user = userRepository.findById(id);
            if (user == null) {
                return ResponseEntity.status(404).body(errorResponse("Пользователь не найден"));
            }

            // Обновляем статус активности и сохраняем в БД
            user.setActive(request.isActive());
            userRepository.update(user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при изменении статуса"));
        }
    }

    // Вспомогательный метод для формирования ответа с ошибкой
    private Map<String, String> errorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }

    // Класс для десериализации запроса на смену роли
    public static class UpdateRoleRequest {
        public String role; // Новая роль: client, metrolog, manager, admin
        public String getRole() { return role; }
    }

    // Класс для десериализации запроса на смену статуса активности
    public static class UpdateActiveRequest {
        public boolean active; // true — активен, false — заблокирован
        public boolean isActive() { return active; }
    }
}