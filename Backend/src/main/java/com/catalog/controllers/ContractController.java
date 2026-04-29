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
// Flow: менеджер создаёт → согласующие проверяют → директор подписывает → клиент подписывает
@RestController
@RequestMapping("/api/contracts")
@CrossOrigin(origins = "http://localhost:5173")
public class ContractController {

    private final ContractRepository contractRepository;
    private final OrderRepository orderRepository;
    private final PdfService pdfService;

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
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Договор не найден"));
            }
            return ResponseEntity.ok(contract);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при получении договора"));
        }
    }

    // POST /api/contracts/{orderId}
    // Менеджер создаёт договор для заявки
    // После создания заявка переходит в статус awaiting_approval
    @PostMapping("/{orderId}")
    public ResponseEntity<?> createContract(@PathVariable int orderId) {
        try {
            Contract existing = contractRepository.findByOrderId(orderId).orElse(null);
            if (existing != null) {
                return ResponseEntity.ok(existing);
            }

            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Заявка не найдена"));
            }

            // Создаём договор со статусом draft
            Contract contract = new Contract(orderId, "CNT-" + System.currentTimeMillis());
            contract.setStatus("pending_approval");
            contractRepository.save(contract);

            // Переводим заявку в статус ожидания согласования
            order.setStatus("awaiting_approval");
            orderRepository.save(order);

            return ResponseEntity.status(201).body(contractRepository.findByOrderId(orderId).orElse(null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при создании договора"));
        }
    }

    // PUT /api/contracts/{orderId}/submit
    // Менеджер отправляет договор на согласование
    @PutMapping("/{orderId}/submit")
    public ResponseEntity<?> submitForApproval(@PathVariable int orderId) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Договор не найден"));
            }

            contract.setStatus("pending_approval");
            contractRepository.save(contract);

            return ResponseEntity.ok(contract);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при отправке на согласование"));
        }
    }

    // PUT /api/contracts/{orderId}/approve
    // Согласующий одобряет договор
    @PutMapping("/{orderId}/approve")
    public ResponseEntity<?> approveContract(@PathVariable int orderId, @RequestBody SignRequest request) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Договор не найден"));
            }

            // После одобрения всех согласующих — переходит к директору
            contract.setStatus("approved");
            contractRepository.save(contract);

            // Обновляем статус заявки
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                order.setStatus("awaiting_director");
                orderRepository.save(order);
            }

            return ResponseEntity.ok(contract);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при согласовании"));
        }
    }

    // PUT /api/contracts/{orderId}/reject
    // Согласующий отклоняет договор — возвращается менеджеру на доработку
    @PutMapping("/{orderId}/reject")
    public ResponseEntity<?> rejectContract(@PathVariable int orderId, @RequestBody RejectRequest request) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Договор не найден"));
            }

            contract.setStatus("rejected");
            contractRepository.save(contract);

            // Возвращаем заявку на pending_contract для доработки менеджером
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                order.setStatus("pending_contract");
                orderRepository.save(order);
            }

            return ResponseEntity.ok(contract);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при отклонении"));
        }
    }

    // PUT /api/contracts/{orderId}/sign/director
    // Директор подписывает договор — после согласования
    @PutMapping("/{orderId}/sign/director")
    public ResponseEntity<?> signByDirector(@PathVariable int orderId, @RequestBody SignRequest request) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Договор не найден"));
            }
            if (!"approved".equals(contract.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Договор должен быть согласован перед подписью директора"));
            }
            if (contract.isDirectorSigned()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Директор уже подписал договор"));
            }

            contract.setDirectorSigned(true);
            contract.setDirectorSignedAt(LocalDateTime.now());
            contract.setDirectorSignedBy(request.getUserId());
            contract.setStatus("signed");
            contractRepository.save(contract);

            // После подписи директора — клиент может оплачивать
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                order.setStatus("awaiting_payment");
                orderRepository.save(order);
            }

            return ResponseEntity.ok(contract);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при подписании"));
        }
    }

    // PUT /api/contracts/{orderId}/sign/client
    // Клиент подписывает договор со своей стороны
    @PutMapping("/{orderId}/sign/client")
    public ResponseEntity<?> signByClient(@PathVariable int orderId, @RequestBody SignRequest request) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Договор не найден"));
            }
            if (!"signed".equals(contract.getStatus()) && !"awaiting_payment".equals(contract.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Договор ещё не готов к подписанию"));
            }
            if (contract.isClientSigned()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Клиент уже подписал договор"));
            }

            contract.setClientSigned(true);
            contract.setClientSignedAt(LocalDateTime.now());
            contract.setClientSignedBy(request.getUserId());
            contractRepository.save(contract);

            return ResponseEntity.ok(contract);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при подписании"));
        }
    }

    // PUT /api/contracts/{orderId}/annul
    // Аннулирование договора — обе стороны
    @PutMapping("/{orderId}/annul")
    public ResponseEntity<?> annulContract(@PathVariable int orderId, @RequestBody RejectRequest request) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Договор не найден"));
            }

            contract.setStatus("annulled");
            contract.setAnnulledAt(LocalDateTime.now());
            contract.setAnnulledBy(request.getUserId());
            contract.setAnnulledReason(request.getReason());
            contractRepository.save(contract);

            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                order.setStatus("annulled");
                orderRepository.save(order);
            }

            return ResponseEntity.ok(contract);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при аннулировании"));
        }
    }

    // PUT /api/contracts/{orderId}/terminate
    // Расторжение договора — одна сторона инициирует
    @PutMapping("/{orderId}/terminate")
    public ResponseEntity<?> terminateContract(@PathVariable int orderId, @RequestBody RejectRequest request) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Договор не найден"));
            }

            contract.setStatus("terminated");
            contract.setTerminatedAt(LocalDateTime.now());
            contract.setTerminatedBy(request.getUserId());
            contract.setTerminatedReason(request.getReason());
            contractRepository.save(contract);

            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                order.setStatus("terminated");
                orderRepository.save(order);
            }

            return ResponseEntity.ok(contract);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при расторжении"));
        }
    }

    // GET /api/contracts/{orderId}/download
    // Генерирует и возвращает PDF договора
    @GetMapping("/{orderId}/download")
    public ResponseEntity<byte[]> downloadContract(@PathVariable int orderId) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            Order order = orderRepository.findById(orderId).orElse(null);

            if (contract == null || order == null) {
                return ResponseEntity.status(404).build();
            }

            byte[] pdfBytes = pdfService.generateContract(order, contract);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "contract_" + orderId + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    public static class SignRequest {
        public Integer userId;
        public Integer getUserId() { return userId; }
    }

    public static class RejectRequest {
        public Integer userId;
        public String reason;
        public Integer getUserId() { return userId; }
        public String getReason() { return reason; }
    }
}