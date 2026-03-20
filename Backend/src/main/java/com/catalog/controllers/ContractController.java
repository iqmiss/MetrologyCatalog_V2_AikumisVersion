package com.catalog.controllers;

import com.catalog.models.Contract;
import com.catalog.models.Order;
import com.catalog.repository.ContractRepository;
import com.catalog.repository.OrderRepository;
import com.catalog.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

// Контроллер для управления договорами
// Договор создаётся автоматически при подаче заявки
// Клиент может скачать PDF и подписать ЭЦП
@RestController
@RequestMapping("/api/contracts")
@CrossOrigin(origins = "http://localhost:5173")
public class ContractController {

    private final ContractRepository contractRepository;
    private final OrderRepository orderRepository;

    // Сервис для генерации PDF договора через iText 7
    private final PdfService pdfService;

    // Spring автоматически передаёт все зависимости через конструктор (Dependency Injection)
    public ContractController(ContractRepository contractRepository,
                               OrderRepository orderRepository,
                               PdfService pdfService) {
        this.contractRepository = contractRepository;
        this.orderRepository = orderRepository;
        this.pdfService = pdfService;
    }

    // GET /api/contracts/{orderId}
    // Возвращает данные договора по ID заявки
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getContract(@PathVariable int orderId) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId);
            if (contract == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Договор не найден"));
            }
            return ResponseEntity.ok(contract);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при получении договора"));
        }
    }

    // POST /api/contracts/{orderId}
    // Создаёт договор для заявки если его ещё нет
    // Вызывается из MyOrders.tsx при нажатии кнопки "📝 Договор"
    @PostMapping("/{orderId}")
    public ResponseEntity<?> createContract(@PathVariable int orderId) {
        try {
            // Проверяем нет ли уже договора для этой заявки (уникальность по order_id)
            Contract existing = contractRepository.findByOrderId(orderId);
            if (existing != null) {
                // Если договор уже есть — возвращаем существующий
                return ResponseEntity.ok(existing);
            }

            // Проверяем что заявка существует
            Order order = orderRepository.findById(orderId);
            if (order == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Заявка не найдена"));
            }

            // Создаём новый договор с уникальным номером
            Contract contract = new Contract();
            contract.setOrderId(orderId);
            contract.setContractNumber("CNT-" + System.currentTimeMillis());
            contract.setSigned(false);
            contractRepository.save(contract);

            // Возвращаем сохранённый договор с присвоенным id
            Contract saved = contractRepository.findByOrderId(orderId);
            return ResponseEntity.status(201).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при создании договора"));
        }
    }

    // PUT /api/contracts/{orderId}/sign
    // Подписывает договор — имитация ЭЦП
    // Вызывается из MyOrders.tsx при нажатии "✍️ Подписать ЭЦП"
    @PutMapping("/{orderId}/sign")
    public ResponseEntity<?> signContract(@PathVariable int orderId, @RequestBody SignRequest request) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId);
            if (contract == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Договор не найден"));
            }

            // Фиксируем подпись: отмечаем как подписанный, сохраняем время и ID подписанта
            contract.setSigned(true);
            contract.setSignedAt(LocalDateTime.now());
            contract.setSignedBy(request.getUserId());
            contractRepository.update(contract);

            return ResponseEntity.ok(contract);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при подписании"));
        }
    }

    // GET /api/contracts/{orderId}/download
    // Генерирует и возвращает PDF договора для скачивания
    // Данные для PDF берутся из таблиц contracts и orders
    @GetMapping("/{orderId}/download")
    public ResponseEntity<byte[]> downloadContract(@PathVariable int orderId) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId);
            Order order = orderRepository.findById(orderId);

            if (contract == null || order == null) {
                return ResponseEntity.status(404).build();
            }

            // Генерируем PDF через PdfService
            byte[] pdfBytes = pdfService.generateContract(order, contract);

            // Устанавливаем заголовки чтобы браузер скачал файл
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "contract_" + orderId + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Класс для десериализации тела запроса при подписании договора
    public static class SignRequest {
        public Integer userId; // ID пользователя который подписывает договор
        public Integer getUserId() { return userId; }
    }
}