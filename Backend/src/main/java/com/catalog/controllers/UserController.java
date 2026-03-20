package com.catalog.controllers;

import com.catalog.models.User;
import com.catalog.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

// Контроллер для управления профилем пользователя
// Обрабатывает запросы по пути /api/profile
@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // GET /api/profile?userId=1
    // Возвращает профиль пользователя по его ID
    @GetMapping
    public ResponseEntity<?> getProfile(@RequestParam int userId) {
        try {
            User user = userRepository.findById(userId);

            // Если пользователь не найден — возвращаем 404
            if (user == null) {
                return ResponseEntity.status(404).body(errorResponse("Пользователь не найден"));
            }

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при получении профиля"));
        }
    }

    // PUT /api/profile
    // Обновляет данные профиля пользователя (ФИО, телефон, email)
    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        try {
            // Проверяем что ID пользователя передан
            if (request.getId() == null) {
                return ResponseEntity.status(400).body(errorResponse("ID пользователя не указан"));
            }

            // Ищем пользователя в БД
            User user = userRepository.findById(request.getId());
            if (user == null) {
                return ResponseEntity.status(404).body(errorResponse("Пользователь не найден"));
            }

            // Обновляем только те поля которые переданы в запросе (частичное обновление)
            if (request.getFullName() != null) {
                user.setFullName(request.getFullName());
            }
            if (request.getPhone() != null) {
                user.setPhone(request.getPhone());
            }
            if (request.getEmail() != null) {
                user.setEmail(request.getEmail());
            }

            // Сохраняем изменения в БД
            userRepository.update(user);

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при обновлении профиля"));
        }
    }

    // Вспомогательный метод для формирования ответа с ошибкой
    private Map<String, String> errorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }

    // Класс для десериализации тела запроса при обновлении профиля
    public static class UpdateProfileRequest {
        public Integer id;
        public String fullName;
        public String email;
        public String phone;

        public Integer getId() { return id; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
    }
}