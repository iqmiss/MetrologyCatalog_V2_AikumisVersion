package com.catalog.controllers;

import com.catalog.models.Laboratory;
import com.catalog.repository.LaboratoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Контроллер для получения списка лабораторий
// Используется в форме создания заявки для выбора лаборатории-исполнителя
@RestController
@RequestMapping("/api/laboratories")
@CrossOrigin(origins = "http://localhost:5173")
public class LaboratoryController {

    private final LaboratoryRepository laboratoryRepository;

    public LaboratoryController(LaboratoryRepository laboratoryRepository) {
        this.laboratoryRepository = laboratoryRepository;
    }

    // GET /api/laboratories
    // Возвращает список всех лабораторий
    // Вызывается из CreateOrder.tsx при загрузке формы создания заявки
    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            List<Laboratory> labs = laboratoryRepository.findAll();
            return ResponseEntity.ok(labs);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при получении лабораторий"));
        }
    }
}