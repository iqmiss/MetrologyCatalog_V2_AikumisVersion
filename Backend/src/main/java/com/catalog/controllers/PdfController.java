package com.catalog.controllers;

import com.catalog.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "http://localhost:5173")
public class PdfController {

    private final PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    // GET /api/pdf/certificate/{orderId}
    // Генерирует PDF сертификат/протокол/отчёт для завершённой заявки
    @GetMapping("/certificate/{orderId}")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable int orderId) {
        byte[] pdfBytes = pdfService.generateCertificate(orderId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "certificate_" + orderId + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    // GET /api/pdf/invoice/{orderId}
    // Генерирует PDF счёт на оплату — доступен финансисту и менеджеру
    // Сумма счёта берётся из order.invoiceAmount (если задана) или order.totalPrice
    @GetMapping("/invoice/{orderId}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable int orderId) {
        byte[] pdfBytes = pdfService.generateInvoice(orderId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice_" + orderId + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}