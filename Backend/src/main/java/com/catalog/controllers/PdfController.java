package com.catalog.controllers;

import com.catalog.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Контроллер для генерации и скачивания PDF документов
// Используется клиентом и метрологом для скачивания сертификатов
@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "http://localhost:5173")
public class PdfController {

    // Сервис который содержит логику генерации PDF через iText 7
    private final PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    // GET /api/pdf/certificate/{orderId}
    // Генерирует PDF сертификат/протокол/отчёт для завершённой заявки
    // Данные берутся из таблиц orders и results
    @GetMapping("/certificate/{orderId}")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable int orderId) {
        // Генерируем PDF в виде массива байт
        byte[] pdfBytes = pdfService.generateCertificate(orderId);

        // Устанавливаем заголовки ответа чтобы браузер скачал файл
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "certificate_" + orderId + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}