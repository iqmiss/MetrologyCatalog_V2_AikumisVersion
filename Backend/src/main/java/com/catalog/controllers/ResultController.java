package com.catalog.controllers;

import com.catalog.models.Result;
import com.catalog.repository.ResultRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

// Контроллер для управления результатами поверки
// Метролог создаёт результат когда завершает заявку
@RestController
@RequestMapping("/api/results")
@CrossOrigin(origins = "http://localhost:5173")
public class ResultController {

    private final ResultRepository resultRepository;

    public ResultController(ResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    // POST /api/results
    // Создаёт запись о результате поверки когда метролог завершает заявку
    // Вызывается из Queue.tsx при нажатии "Завершить" в модалке
    @PostMapping
    public ResponseEntity<?> createResult(@RequestBody CreateResultRequest request) {
        try {
            // Создаём объект результата и заполняем поля
            Result result = new Result();
            result.setOrderId(request.getOrderId());       // ID заявки
            result.setResultType(request.getResultType()); // Тип: certificate, protocol, report
            result.setMetrologistId(request.getMetrologistId()); // ID метролога который провёл работу
            result.setIssuedAt(LocalDateTime.now());       // Дата выдачи — текущее время
            result.setSigned(true);                        // Считается подписанным сразу
            result.setSignedAt(LocalDateTime.now());       // Дата подписания — текущее время

            // Сохраняем результат в БД
            resultRepository.save(result);

            return ResponseEntity.status(201).body(Map.of("message", "Результат создан"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при создании результата"));
        }
    }

    // Класс для десериализации тела запроса при создании результата
    public static class CreateResultRequest {
        public int orderId;       // ID заявки
        public String resultType; // Тип документа: certificate, protocol, report
        public int metrologistId; // ID метролога

        public int getOrderId() { return orderId; }
        public String getResultType() { return resultType; }
        public int getMetrologistId() { return metrologistId; }
    }
}