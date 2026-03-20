package com.catalog.controllers;

import com.catalog.models.Service;
import com.catalog.repository.ServiceRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

// Контроллер для работы с каталогом метрологических услуг
// Обрабатывает запросы по пути /api/services
@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "http://localhost:5173")
public class ServiceController {

    private final ServiceRepository serviceRepository;

    public ServiceController(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    // GET /api/services
    // Возвращает список всех активных услуг
    // labName заполняется через @Transient — не хранится в БД
    @GetMapping
    public ResponseEntity<?> getAllServices() {
        try {
            List<Service> services = serviceRepository.findByIsActiveTrue();
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при получении услуг"));
        }
    }

    // GET /api/services/{id}
    // Возвращает одну услугу по её ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getServiceById(@PathVariable int id) {
        try {
            Service service = serviceRepository.findById(id).orElse(null);

            // Если услуга не найдена — возвращаем 404
            if (service == null) {
                return ResponseEntity.status(404).body(errorResponse("Услуга не найдена"));
            }

            return ResponseEntity.ok(service);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при получении услуги"));
        }
    }

    // GET /api/services/type/{measurementType}
    // Фильтрует услуги по типу средства измерений (например: Манометр, Термометр)
    // Используется в каталоге для фильтрации
    @GetMapping("/type/{measurementType}")
    public ResponseEntity<?> getByMeasurementType(@PathVariable String measurementType) {
        try {
            List<Service> services = serviceRepository.findByMeasurementTypeAndIsActiveTrue(measurementType);
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при фильтрации услуг"));
        }
    }

    // GET /api/services/lab/{labId}
    // Возвращает все услуги конкретной лаборатории
    @GetMapping("/lab/{labId}")
    public ResponseEntity<?> getByLabId(@PathVariable int labId) {
        try {
            List<Service> services = serviceRepository.findByLabIdAndIsActiveTrue(labId);
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при получении услуг лаборатории"));
        }
    }

    // Вспомогательный метод для формирования ответа с ошибкой
    private java.util.Map<String, String> errorResponse(String message) {
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", message);
        return response;
    }
}