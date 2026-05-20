package com.catalog.controllers;

import com.catalog.service.ApplicationCodeService;

import com.catalog.models.Contract;
import com.catalog.models.Order;
import com.catalog.repository.ContractRepository;
import com.catalog.repository.OrderRepository;
import com.catalog.service.NotificationService;
import com.catalog.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
@CrossOrigin(origins = "http://localhost:5173")
public class ContractController {

    private final ContractRepository contractRepository;
    private final OrderRepository orderRepository;
    private final PdfService pdfService;
    private final NotificationService notificationService;
    private final ApplicationCodeService applicationCodeService;

    public ContractController(ContractRepository contractRepository,
                                   OrderRepository orderRepository,
                                   PdfService pdfService,
                                   NotificationService notificationService,
                                   ApplicationCodeService applicationCodeService) {
            this.contractRepository = contractRepository;
            this.orderRepository = orderRepository;
            this.pdfService = pdfService;
            this.notificationService = notificationService;
            this.applicationCodeService = applicationCodeService;
        }

    // GET /api/contracts/{orderId}
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getContract(@PathVariable int orderId) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null)
                return ResponseEntity.status(404).body(Map.of("message", "Договор не найден"));
            Contract response = copyWithoutFile(contract);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при получении договора"));
        }
    }

    // POST /api/contracts/{orderId}
    // Менеджер загружает файл договора и отправляет на согласование тройке
    @PostMapping("/{orderId}")
    public ResponseEntity<?> uploadContract(@PathVariable int orderId,
                                             @RequestBody UploadContractRequest request) {
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null)
                return ResponseEntity.status(404).body(Map.of("message", "Заявка не найдена"));

            if (request.getFileData() == null || request.getFileData().isBlank())
                return ResponseEntity.status(400).body(Map.of("message", "Файл договора обязателен"));
            if (request.getFileName() == null || request.getFileName().isBlank())
                return ResponseEntity.status(400).body(Map.of("message", "Имя файла обязательно"));
            if (request.getFileData().length() > 10_000_000)
                return ResponseEntity.status(400).body(Map.of("message", "Файл слишком большой. Максимум 7MB"));

            Contract contract = contractRepository.findByOrderId(orderId)
                .orElse(new Contract(orderId, "CNT-" + System.currentTimeMillis()));

                    if ("pending_approval".equals(contract.getStatus()) || "signed".equals(contract.getStatus()))
                return ResponseEntity.ok(copyWithoutFile(contract));

            contract.setContractFile(request.getFileData());
            contract.setContractFileName(request.getFileName());
            contract.setStatus("pending_approval");
            contract.setApproverSigned(false); contract.setApproverSignedAt(null); contract.setApproverSignedBy(null);
            contract.setFinancierSigned(false); contract.setFinancierSignedAt(null); contract.setFinancierSignedBy(null);
            contract.setDirectorSigned(false); contract.setDirectorSignedAt(null); contract.setDirectorSignedBy(null);
            contract.setClientSigned(false); contract.setClientSignedAt(null); contract.setClientSignedBy(null);
            contract.setGenDirectorSigned(false); contract.setGenDirectorSignedAt(null); contract.setGenDirectorSignedBy(null);
            contract.setRejectedByRole(null); contract.setRejectedReason(null);
            contractRepository.save(contract);

            order.setStatus("awaiting_approval");
            orderRepository.save(order);

            notificationService.notifyParallelApprovers(orderId, order.getOrderNumber());

            return ResponseEntity.ok(copyWithoutFile(contractRepository.findByOrderId(orderId).orElse(contract)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при загрузке договора: " + e.getMessage()));
        }
    }

    // GET /api/contracts/{orderId}/file
    // Скачать файл договора (все роли которым нужно подписать)
    @GetMapping("/{orderId}/file")
    public ResponseEntity<?> downloadContractFile(@PathVariable int orderId) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null)
                return ResponseEntity.status(404).body(Map.of("message", "Договор не найден"));
            if (contract.getContractFile() == null)
                return ResponseEntity.status(404).body(Map.of("message", "Файл договора ещё не загружен"));

            byte[] fileBytes = Base64.getDecoder().decode(contract.getContractFile());
            String fileName = contract.getContractFileName() != null
                ? contract.getContractFileName()
                : "contract_" + orderId + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(fileName.endsWith(".pdf") ? MediaType.APPLICATION_PDF : MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(fileBytes.length);
            return ResponseEntity.ok().headers(headers).body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при скачивании файла"));
        }
    }

    // PUT /api/contracts/{orderId}/submit — повторная отправка после rejected
    @PutMapping("/{orderId}/submit")
    public ResponseEntity<?> resubmitForApproval(@PathVariable int orderId) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null)
                return ResponseEntity.status(404).body(Map.of("message", "Договор не найден"));
            if (contract.getContractFile() == null)
                return ResponseEntity.status(400).body(Map.of("message", "Сначала загрузите файл договора"));

            contract.setStatus("pending_approval");
            contract.setApproverSigned(false); contract.setApproverSignedAt(null); contract.setApproverSignedBy(null);
            contract.setFinancierSigned(false); contract.setFinancierSignedAt(null); contract.setFinancierSignedBy(null);
            contract.setDirectorSigned(false); contract.setDirectorSignedAt(null); contract.setDirectorSignedBy(null);
            contract.setClientSigned(false); contract.setClientSignedAt(null); contract.setClientSignedBy(null);
            contract.setGenDirectorSigned(false); contract.setGenDirectorSignedAt(null); contract.setGenDirectorSignedBy(null);
            contract.setRejectedByRole(null); contract.setRejectedReason(null);
            contractRepository.save(contract);

            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                order.setStatus("awaiting_approval");
                orderRepository.save(order);
                notificationService.notifyParallelApprovers(orderId, order.getOrderNumber());
            }

            return ResponseEntity.ok(copyWithoutFile(contract));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при отправке на согласование"));
        }
    }

    // ── Шаг 2: Параллельная тройка ────────────────────────────────────────────

    @PutMapping("/{orderId}/sign/approver")
    public ResponseEntity<?> signByApprover(@PathVariable int orderId, @RequestBody SignRequest req) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null) return notFound();
            if (!"pending_approval".equals(contract.getStatus()))
                return ResponseEntity.badRequest().body(Map.of("message", "Договор не на согласовании"));
            if (contract.getContractFile() == null)
                return ResponseEntity.badRequest().body(Map.of("message", "Менеджер ещё не загрузил файл договора"));
            if (contract.isApproverSigned())
                return ResponseEntity.badRequest().body(Map.of("message", "Согласующий уже подписал"));

            contract.setApproverSigned(true);
            contract.setApproverSignedAt(LocalDateTime.now());
            contract.setApproverSignedBy(req.userId);
            contractRepository.save(contract);

            checkTrioAndNotifyClient(contract, orderId);
            return ResponseEntity.ok(copyWithoutFile(contractRepository.findByOrderId(orderId).orElse(contract)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при подписании"));
        }
    }

    @PutMapping("/{orderId}/sign/financier")
    public ResponseEntity<?> signByFinancier(@PathVariable int orderId, @RequestBody SignRequest req) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null) return notFound();
            if (!"pending_approval".equals(contract.getStatus()))
                return ResponseEntity.badRequest().body(Map.of("message", "Договор не на согласовании"));
            if (contract.getContractFile() == null)
                return ResponseEntity.badRequest().body(Map.of("message", "Менеджер ещё не загрузил файл договора"));
            if (contract.isFinancierSigned())
                return ResponseEntity.badRequest().body(Map.of("message", "Финансист уже подписал"));

            contract.setFinancierSigned(true);
            contract.setFinancierSignedAt(LocalDateTime.now());
            contract.setFinancierSignedBy(req.userId);
            contractRepository.save(contract);

            checkTrioAndNotifyClient(contract, orderId);
            return ResponseEntity.ok(copyWithoutFile(contractRepository.findByOrderId(orderId).orElse(contract)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при подписании"));
        }
    }

    @PutMapping("/{orderId}/sign/director")
    public ResponseEntity<?> signByDirector(@PathVariable int orderId, @RequestBody SignRequest req) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null) return notFound();
            if (!"pending_approval".equals(contract.getStatus()))
                return ResponseEntity.badRequest().body(Map.of("message", "Договор не на согласовании"));
            if (contract.getContractFile() == null)
                return ResponseEntity.badRequest().body(Map.of("message", "Менеджер ещё не загрузил файл договора"));
            if (contract.isDirectorSigned())
                return ResponseEntity.badRequest().body(Map.of("message", "Директор уже подписал"));

            contract.setDirectorSigned(true);
            contract.setDirectorSignedAt(LocalDateTime.now());
            contract.setDirectorSignedBy(req.userId);
            contractRepository.save(contract);

            checkTrioAndNotifyClient(contract, orderId);
            return ResponseEntity.ok(copyWithoutFile(contractRepository.findByOrderId(orderId).orElse(contract)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при подписании"));
        }
    }

    // ── Шаг 3: Клиент ─────────────────────────────────────────────────────────

    @PutMapping("/{orderId}/sign/client")
    public ResponseEntity<?> signByClient(@PathVariable int orderId, @RequestBody SignRequest req) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null) return notFound();
            if (!contract.isTrioSigned())
                return ResponseEntity.badRequest().body(Map.of("message", "Договор ещё не подписан всеми сторонами организации"));
            if (contract.isClientSigned())
                return ResponseEntity.badRequest().body(Map.of("message", "Клиент уже подписал"));

            contract.setClientSigned(true);
            contract.setClientSignedAt(LocalDateTime.now());
            contract.setClientSignedBy(req.userId);
            contractRepository.save(contract);

            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null)
                notificationService.notifyGenDirectorForSigning(orderId, order.getOrderNumber());

            return ResponseEntity.ok(copyWithoutFile(contract));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при подписании"));
        }
    }

    // ── Шаг 4: Ген.директор (финальная подпись) ───────────────────────────────

    @PutMapping("/{orderId}/sign/gen_director")
    public ResponseEntity<?> signByGenDirector(@PathVariable int orderId, @RequestBody SignRequest req) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null) return notFound();
            if (!contract.isTrioSigned())
                return ResponseEntity.badRequest().body(Map.of("message", "Тройка ещё не подписала договор"));
            if (!contract.isClientSigned())
                return ResponseEntity.badRequest().body(Map.of("message", "Клиент ещё не подписал договор"));
            if (contract.isGenDirectorSigned())
                return ResponseEntity.badRequest().body(Map.of("message", "Ген.директор уже подписал"));

            contract.setGenDirectorSigned(true);
            contract.setGenDirectorSignedAt(LocalDateTime.now());
            contract.setGenDirectorSignedBy(req.userId);
            contract.setStatus("signed");
            contractRepository.save(contract);

            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                String appCode = applicationCodeService.generateCode(order);
                contract.setRegistrationNumber(appCode);
                order.setApplicationCode(appCode);
                order.setStatus("awaiting_payment");
                orderRepository.save(order);
                contractRepository.save(contract);
                notificationService.notifyFinanciersContractSigned(orderId, order.getOrderNumber());
            }

            return ResponseEntity.ok(copyWithoutFile(contract));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при подписании"));
        }
    }

    // ── Отклонение ────────────────────────────────────────────────────────────

    @PutMapping("/{orderId}/reject")
    public ResponseEntity<?> rejectContract(@PathVariable int orderId, @RequestBody RejectRequest req) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null) return notFound();

            contract.setStatus("rejected");
            contract.setRejectedByRole(req.role != null ? req.role : "unknown");
            contract.setRejectedReason(req.reason);
            contractRepository.save(contract);

            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                order.setStatus("pending_contract");
                orderRepository.save(order);
                notificationService.notifyManagersRejected(orderId, order.getOrderNumber(),
                    req.reason != null ? req.reason : "Причина не указана");
            }

            return ResponseEntity.ok(copyWithoutFile(contract));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при отклонении"));
        }
    }

    @PutMapping("/{orderId}/approve")
    public ResponseEntity<?> approveContract(@PathVariable int orderId, @RequestBody SignRequest req) {
        return signByApprover(orderId, req);
    }

    @PutMapping("/{orderId}/annul")
    public ResponseEntity<?> annulContract(@PathVariable int orderId, @RequestBody RejectRequest req) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null) return notFound();
            contract.setStatus("annulled");
            contract.setAnnulledAt(LocalDateTime.now());
            contract.setAnnulledBy(req.userId);
            contract.setAnnulledReason(req.reason);
            contractRepository.save(contract);
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) { order.setStatus("annulled"); orderRepository.save(order); }
            return ResponseEntity.ok(copyWithoutFile(contract));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при аннулировании"));
        }
    }

    @PutMapping("/{orderId}/terminate")
    public ResponseEntity<?> terminateContract(@PathVariable int orderId, @RequestBody RejectRequest req) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null) return notFound();
            contract.setStatus("terminated");
            contract.setTerminatedAt(LocalDateTime.now());
            contract.setTerminatedBy(req.userId);
            contract.setTerminatedReason(req.reason);
            contractRepository.save(contract);
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) { order.setStatus("terminated"); orderRepository.save(order); }
            return ResponseEntity.ok(copyWithoutFile(contract));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при расторжении"));
        }
    }

    // GET /api/contracts/{orderId}/download — генерирует PDF из данных (fallback если нет загруженного файла)
    @GetMapping("/{orderId}/download")
    public ResponseEntity<?> downloadContract(@PathVariable int orderId) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            Order order = orderRepository.findById(orderId).orElse(null);
            if (contract == null || order == null)
                return ResponseEntity.status(404).body(Map.of("message", "Договор не найден"));

            if (contract.getContractFile() != null) {
                byte[] fileBytes = Base64.getDecoder().decode(contract.getContractFile());
                String fileName = contract.getContractFileName() != null
                    ? contract.getContractFileName() : "contract_" + orderId + ".pdf";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", fileName);
                headers.setContentLength(fileBytes.length);
                return ResponseEntity.ok().headers(headers).body(fileBytes);
            }

            byte[] pdfBytes = pdfService.generateContract(order, contract);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "contract_" + orderId + ".pdf");
            headers.setContentLength(pdfBytes.length);
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при скачивании договора"));
        }
    }

    // ── Confirmation round-trip (Step 7) ─────────────────────────────────────

    // PUT /api/contracts/{orderId}/request-confirmations
    // Бухгалтер requests confirmations from metrolog, financier, yurist
    @PutMapping("/{orderId}/request-confirmations")
    public ResponseEntity<?> requestConfirmations(@PathVariable int orderId) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null) return notFound();

            contract.setMetrologConfirmed(false);
            contract.setFinancierConfirmed(false);
            contract.setYuristConfirmed(false);
            contract.setConfirmationsRequested(true);
            contractRepository.save(contract);

            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null)
                notificationService.notifyInternalReviewersConfirmationRequested(
                    orderId, order.getOrderNumber());

            return ResponseEntity.ok(copyWithoutFile(contract));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при запросе подтверждений"));
        }
    }

    // PUT /api/contracts/{orderId}/confirm?role=metrolog
    // One of the 3 roles confirms
    @PutMapping("/{orderId}/confirm")
    public ResponseEntity<?> confirmByRole(
            @PathVariable int orderId,
            @RequestParam String role) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null) return notFound();
            if (!contract.isConfirmationsRequested())
                return ResponseEntity.badRequest().body(Map.of("message", "Подтверждение ещё не запрошено"));

            switch (role.toLowerCase()) {
                case "metrolog" -> contract.setMetrologConfirmed(true);
                case "financier" -> contract.setFinancierConfirmed(true);
                case "yurist" -> contract.setYuristConfirmed(true);
                default -> { return ResponseEntity.badRequest().body(Map.of("message", "Неизвестная роль: " + role)); }
            }
            contractRepository.save(contract);

            if (contract.isAllConfirmed()) {
                Order order = orderRepository.findById(orderId).orElse(null);
                if (order != null)
                    notificationService.notifySignerForFinalSign(orderId, order.getOrderNumber());
            }

            return ResponseEntity.ok(copyWithoutFile(contract));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при подтверждении"));
        }
    }

    // PUT /api/contracts/{orderId}/reject-confirmation?role=metrolog
    // Any of the 3 rejects — resets all confirmations
    @PutMapping("/{orderId}/reject-confirmation")
    public ResponseEntity<?> rejectConfirmation(
            @PathVariable int orderId,
            @RequestParam String role) {
        try {
            Contract contract = contractRepository.findByOrderId(orderId).orElse(null);
            if (contract == null) return notFound();

            contract.setMetrologConfirmed(false);
            contract.setFinancierConfirmed(false);
            contract.setYuristConfirmed(false);
            contract.setConfirmationsRequested(false);
            contractRepository.save(contract);

            return ResponseEntity.ok(copyWithoutFile(contract));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка при отклонении подтверждения"));
        }
    }
    // ── Helpers ───────────────────────────────────────────────────────────────

    private void checkTrioAndNotifyClient(Contract contract, int orderId) {
        Contract fresh = contractRepository.findByOrderId(orderId).orElse(contract);
        if (fresh.isTrioSigned()) {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null)
                notificationService.notifyClientTrioSigned(order.getClientId(), orderId, order.getOrderNumber());
        }
    }

    private Contract copyWithoutFile(Contract c) {
        Contract copy = new Contract();
        copy.setId(c.getId());
        copy.setOrderId(c.getOrderId());
        copy.setContractNumber(c.getContractNumber());
        copy.setRegistrationNumber(c.getRegistrationNumber());
        copy.setContractFileName(c.getContractFileName());
        copy.setFilePath(c.getFilePath());
        copy.setStatus(c.getStatus());
        copy.setDirectorSigned(c.isDirectorSigned());
        copy.setDirectorSignedAt(c.getDirectorSignedAt());
        copy.setApproverSigned(c.isApproverSigned());
        copy.setApproverSignedAt(c.getApproverSignedAt());
        copy.setFinancierSigned(c.isFinancierSigned());
        copy.setFinancierSignedAt(c.getFinancierSignedAt());
        copy.setClientSigned(c.isClientSigned());
        copy.setClientSignedAt(c.getClientSignedAt());
        copy.setGenDirectorSigned(c.isGenDirectorSigned());
        copy.setGenDirectorSignedAt(c.getGenDirectorSignedAt());
        copy.setRejectedByRole(c.getRejectedByRole());
        copy.setRejectedReason(c.getRejectedReason());
        return copy;
    }

    private ResponseEntity<?> notFound() {
        return ResponseEntity.status(404).body(Map.of("message", "Договор не найден"));
    }

    // ── Request classes ───────────────────────────────────────────────────────

    public static class UploadContractRequest {
        public String fileData;
        public String fileName;
        public String getFileData() { return fileData; }
        public String getFileName() { return fileName; }
    }

    public static class SignRequest {
        public Integer userId;
    }

    public static class RejectRequest {
        public Integer userId;
        public String reason;
        public String role;
    }
}