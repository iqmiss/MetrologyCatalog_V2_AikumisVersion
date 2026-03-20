package com.catalog.controllers;

import com.catalog.models.User;
import com.catalog.models.Company;
import com.catalog.repository.UserRepository;
import com.catalog.service.EmailService;
import com.catalog.utils.JwtUtil;
import com.catalog.repository.CompanyRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

// Контроллер для аутентификации и авторизации пользователей
// Обрабатывает вход, регистрацию и восстановление пароля
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final EmailService emailService; // Сервис для отправки email (восстановление пароля)
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // Энкодер для хэширования паролей через алгоритм BCrypt
    private final JwtUtil jwtUtil;

    // Хранилище токенов для сброса пароля в памяти
    // Ключ — UUID токен, значение — email пользователя
    // Сбрасывается при перезапуске сервера
    private static final Map<String, String> resetTokens = new HashMap<>();

    // Spring автоматически передаёт все зависимости через конструктор (Dependency Injection)
    public AuthController(UserRepository userRepository,
                          CompanyRepository companyRepository,
                          EmailService emailService,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
    }

    // POST /api/auth/login
    // Аутентификация пользователя по email и паролю
    // Возвращает токен и данные пользователя
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Валидация входных данных
        if (request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.status(400).body(errorResponse("Email и пароль обязательны"));
        }

        // Ищем пользователя по email
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            return ResponseEntity.status(401).body(errorResponse("Пользователь не найден"));
        }

        // Проверяем что аккаунт не заблокирован
        if (!user.isActive()) {
            return ResponseEntity.status(401).body(errorResponse("Пользователь неактивен"));
        }

        // Сравниваем введённый пароль с хэшем в БД через BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(errorResponse("Неверный пароль"));
        }

        // Генерируем токен сессии и возвращаем данные пользователя
        String token = generateToken(user);
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", user);

        return ResponseEntity.ok(response);
    }

    // POST /api/auth/register
    // Регистрация нового клиента (юридического лица)
    // Если переданы данные компании — создаёт запись в таблице companies
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Валидация обязательных полей
        if (request.getEmail() == null || request.getPassword() == null || request.getFullName() == null) {
            return ResponseEntity.status(400).body(errorResponse("Email, пароль и ФИО обязательны"));
        }

        if (request.getPassword().length() < 6) {
            return ResponseEntity.status(400).body(errorResponse("Пароль должен быть не менее 6 символов"));
        }

        // Проверяем что email ещё не зарегистрирован
        User existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser != null) {
            return ResponseEntity.status(409).body(errorResponse("Email уже зарегистрирован"));
        }

        // Если переданы БИН и название компании — создаём запись о компании
        Integer companyId = null;
        if (request.getBin() != null && request.getCompanyName() != null) {
            Company company = new Company();
            company.setBin(request.getBin());
            company.setName(request.getCompanyName());
            company.setAddress(request.getCompanyAddress());
            company.setPhone(request.getPhone());
            company.setEmail(request.getEmail());
            companyRepository.save(company);

            // Получаем сохранённую компанию чтобы узнать её id
            Company savedCompany = companyRepository.findByBin(request.getBin());
            if (savedCompany != null) {
                companyId = savedCompany.getId();
            }
        }

        // Создаём пользователя с хэшированным паролем
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // BCrypt хэширование
        user.setRole("client"); // Все новые пользователи получают роль client
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setCompanyId(companyId);
        user.setActive(true);
        userRepository.save(user);

        // Возвращаем токен и данные созданного пользователя
        User createdUser = userRepository.findByEmail(request.getEmail());
        String token = generateToken(createdUser);
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", createdUser);

        return ResponseEntity.status(201).body(response);
    }

    // POST /api/auth/forgot-password
    // Отправляет ссылку для сброса пароля на email
    // Генерирует UUID токен и сохраняет его в памяти
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        if (request.getEmail() == null) {
            return ResponseEntity.status(400).body(errorResponse("Email обязателен"));
        }

        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            // Не раскрываем что пользователь не найден — защита от перебора email
            return ResponseEntity.ok(Map.of("message", "Ссылка отправлена на email если он зарегистрирован"));
        }

        // Генерируем уникальный токен и сохраняем связку токен→email
        String token = java.util.UUID.randomUUID().toString();
        resetTokens.put(token, request.getEmail());

        // Формируем ссылку для сброса пароля и отправляем на email
        String resetLink = "http://localhost:5173/reset-password?token=" + token;
        emailService.sendPasswordReset(request.getEmail(), user.getFullName(), resetLink);

        return ResponseEntity.ok(Map.of("message", "Ссылка отправлена на email если он зарегистрирован"));
    }

    // POST /api/auth/reset-password
    // Сбрасывает пароль по токену из email
    // После успешного сброса токен удаляется из памяти
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request.getToken() == null || request.getPassword() == null) {
            return ResponseEntity.status(400).body(errorResponse("Токен и пароль обязательны"));
        }

        // Ищем email по токену
        String email = resetTokens.get(request.getToken());
        if (email == null) {
            return ResponseEntity.status(400).body(errorResponse("Недействительный или устаревший токен"));
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(404).body(errorResponse("Пользователь не найден"));
        }

        // Обновляем пароль с BCrypt хэшированием и удаляем использованный токен
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.update(user);
        resetTokens.remove(request.getToken());

        return ResponseEntity.ok(Map.of("message", "Пароль успешно изменён"));
    }

    // Генерирует простой токен сессии на основе ID пользователя и текущего времени
    private String generateToken(User user) {
        return jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
    }

    // Вспомогательный метод для формирования ответа с ошибкой
    private Map<String, String> errorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }

    // Класс для десериализации запроса на вход
    public static class LoginRequest {
        public String email;
        public String password;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // Класс для десериализации запроса на регистрацию
    // Содержит как данные пользователя так и данные компании (юридического лица)
    public static class RegisterRequest {
        public String email;
        public String password;
        public String fullName;
        public String phone;
        public String bin;          // БИН компании
        public String companyName;  // Название компании
        public String companyAddress;
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getFullName() { return fullName; }
        public String getPhone() { return phone; }
        public String getBin() { return bin; }
        public String getCompanyName() { return companyName; }
        public String getCompanyAddress() { return companyAddress; }
    }

    // Класс для десериализации запроса на восстановление пароля
    public static class ForgotPasswordRequest {
        public String email;
        public String getEmail() { return email; }
    }

    // Класс для десериализации запроса на сброс пароля
    public static class ResetPasswordRequest {
        public String token;       // UUID токен из email ссылки
        public String newPassword; // Новый пароль
        public String getToken() { return token; }
        public String getPassword() { return newPassword; }
    }
}