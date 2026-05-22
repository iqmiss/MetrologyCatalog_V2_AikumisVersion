package com.catalog.controllers;

import com.catalog.models.User;
import com.catalog.models.Company;
import com.catalog.repository.UserRepository;
import com.catalog.repository.CompanyRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public UserController(UserRepository userRepository, CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    // GET /api/profile?userId=1
    @GetMapping
    public ResponseEntity<?> getProfile(@RequestParam int userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null)
                return ResponseEntity.status(404).body(errorResponse("Пользователь не найден"));

            Map<String, Object> response = new HashMap<>();
            response.put("user", user);

            if (user.getCompanyId() != null) {
                Company company = companyRepository.findById(user.getCompanyId()).orElse(null);
                response.put("company", company);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при получении профиля"));
        }
    }

    // PUT /api/profile
    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        try {
            if (request.getId() == null)
                return ResponseEntity.status(400).body(errorResponse("ID пользователя не указан"));

            User user = userRepository.findById(request.getId()).orElse(null);
            if (user == null)
                return ResponseEntity.status(404).body(errorResponse("Пользователь не найден"));

            if (request.getFullName() != null) user.setFullName(request.getFullName());
            if (request.getPhone() != null) user.setPhone(request.getPhone());
            if (request.getEmail() != null) user.setEmail(request.getEmail());
            if (request.getIin() != null) user.setIin(request.getIin());
            userRepository.save(user);

            // Update company fields if provided
            if (user.getCompanyId() != null && request.getCompany() != null) {
                Company company = companyRepository.findById(user.getCompanyId()).orElse(null);
                if (company != null) {
                    UpdateCompanyRequest c = request.getCompany();
                    if (c.getDirectorName() != null) company.setDirectorName(c.getDirectorName());
                    if (c.getDirectorPosition() != null) company.setDirectorPosition(c.getDirectorPosition());
                    if (c.getIik() != null) company.setIik(c.getIik());
                    if (c.getBankName() != null) company.setBankName(c.getBankName());
                    if (c.getBik() != null) company.setBik(c.getBik());
                    if (c.getKbe() != null) company.setKbe(c.getKbe());
                    if (c.getLegalAddress() != null) company.setLegalAddress(c.getLegalAddress());
                    if (c.getAddress() != null) company.setAddress(c.getAddress());
                    if (c.getPhone() != null) company.setPhone(c.getPhone());
                    companyRepository.save(company);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            if (user.getCompanyId() != null) {
                companyRepository.findById(user.getCompanyId()).ifPresent(c -> response.put("company", c));
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при обновлении профиля"));
        }
    }

    private Map<String, String> errorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }

    public static class UpdateProfileRequest {
        public Integer id;
        public String fullName, email, phone, iin;
        public UpdateCompanyRequest company;

        public Integer getId() { return id; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getIin() { return iin; }
        public UpdateCompanyRequest getCompany() { return company; }
    }

    public static class UpdateCompanyRequest {
        public String directorName, directorPosition, iik, bankName, bik, kbe, legalAddress, address, phone;

        public String getDirectorName() { return directorName; }
        public String getDirectorPosition() { return directorPosition; }
        public String getIik() { return iik; }
        public String getBankName() { return bankName; }
        public String getBik() { return bik; }
        public String getKbe() { return kbe; }
        public String getLegalAddress() { return legalAddress; }
        public String getAddress() { return address; }
        public String getPhone() { return phone; }
    }
}