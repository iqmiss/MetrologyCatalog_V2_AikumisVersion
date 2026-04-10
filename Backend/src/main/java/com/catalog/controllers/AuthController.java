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

import java.time.LocalDateTime;
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
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;

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
    // Возвращает JWT токен и данные пользователя
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.status(400).body(errorResponse("Email и пароль обязательны"));
        }

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(errorResponse("Пользователь не найден"));
        }

        if (!user.isActive()) {
            return ResponseEntity.status(401).body(errorResponse("Пользователь неактивен"));
        }

        // Сравниваем введённый пароль с хэшем в БД через BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(errorResponse("Неверный пароль"));
        }

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
        if (request.getEmail() == null || request.getPassword() == null || request.getFullName() == null) {
            return ResponseEntity.status(400).body(errorResponse("Email, пароль и ФИО обязательны"));
        }

        if (request.getPassword().length() < 6) {
            return ResponseEntity.status(400).body(errorResponse("Пароль должен быть не менее 6 символов"));
        }

        User existingUser = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (existingUser != null) {
            return ResponseEntity.status(409).body(errorResponse("Email уже зарегистрирован"));
        }

        Integer companyId = null;
        if (request.getBin() != null && request.getCompanyName() != null) {
            Company company = new Company();
            company.setBin(request.getBin());
            company.setName(request.getCompanyName());
            company.setAddress(request.getCompanyAddress());
            company.setPhone(request.getPhone());
            company.setEmail(request.getEmail());
            companyRepository.save(company);

            Company savedCompany = companyRepository.findByBin(request.getBin()).orElse(null);
            if (savedCompany != null) {
                companyId = savedCompany.getId();
            }
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("client");
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setCompanyId(companyId);
        user.setActive(true);
        userRepository.save(user);

        User createdUser = userRepository.findByEmail(request.getEmail()).orElse(null);
        String token = generateToken(createdUser);
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", createdUser);

        return ResponseEntity.status(201).body(response);
    }

    // POST /api/auth/forgot-password
    // Генерирует UUID токен, сохраняет в БД с временем истечения (24 часа)
    // и отправляет ссылку для сброса пароля на email
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        if (request.getEmail() == null) {
            return ResponseEntity.status(400).body(errorResponse("Email обязателен"));
        }

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            // Не раскрываем что пользователь не найден — защита от перебора email
            return ResponseEntity.ok(Map.of("message", "Ссылка отправлена на email если он зарегистрирован"));
        }

        // Генерируем токен и сохраняем в БД с временем истечения 24 часа
        String token = java.util.UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetExpires(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        String resetLink = "http://localhost:5173/reset-password?token=" + token;
        emailService.sendPasswordReset(request.getEmail(), user.getFullName(), resetLink);

        return ResponseEntity.ok(Map.of("message", "Ссылка отправлена на email если он зарегистрирован"));
    }

    // POST /api/auth/reset-password
    // Ищет токен в БД, проверяет время истечения
    // После успешного сброса очищает токен из БД
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request.getToken() == null || request.getPassword() == null) {
            return ResponseEntity.status(400).body(errorResponse("Токен и пароль обязательны"));
        }

        // Ищем пользователя по токену в БД
        User user = userRepository.findByPasswordResetToken(request.getToken()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(400).body(errorResponse("Недействительный токен"));
        }

        // Проверяем что токен ещё не истёк
        if (user.getPasswordResetExpires() == null ||
            user.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(400).body(errorResponse("Срок действия ссылки истёк"));
        }

        // Обновляем пароль и очищаем токен из БД
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Пароль успешно изменён"));
    }

    // Генерирует JWT токен с данными пользователя
    private String generateToken(User user) {
        return jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole(), user.getLabId());
    }

    // Вспомогательный метод для формирования ответа с ошибкой
    private Map<String, String> errorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }

    public static class LoginRequest {
        public String email;
        public String password;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        public String email;
        public String password;
        public String fullName;
        public String phone;
        public String bin;
        public String companyName;
        public String companyAddress;
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getFullName() { return fullName; }
        public String getPhone() { return phone; }
        public String getBin() { return bin; }
        public String getCompanyName() { return companyName; }
        public String getCompanyAddress() { return companyAddress; }
    }

    public static class ForgotPasswordRequest {
        public String email;
        public String getEmail() { return email; }
    }

    public static class ResetPasswordRequest {
        public String token;
        public String newPassword;
        public String getToken() { return token; }
        public String getPassword() { return newPassword; }
    }
}