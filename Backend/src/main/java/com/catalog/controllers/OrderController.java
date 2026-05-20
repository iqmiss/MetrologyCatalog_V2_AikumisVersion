package com.catalog.controllers;

import com.catalog.models.User;
import com.catalog.models.Order;
import com.catalog.models.Contract;
import com.catalog.models.OrderItem;
import com.catalog.models.Laboratory;
import com.catalog.repository.UserRepository;
import com.catalog.repository.OrderRepository;
import com.catalog.repository.ContractRepository;
import com.catalog.repository.OrderItemRepository;
import com.catalog.repository.LaboratoryRepository;
import com.catalog.service.EmailService;
import com.catalog.service.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final com.catalog.repository.ApplicationFieldValueRepository applicationFieldValueRepository;

    public OrderController(OrderRepository orderRepository,
                               OrderItemRepository orderItemRepository,
                               UserRepository userRepository,
                               EmailService emailService,
                               ContractRepository contractRepository,
                               LaboratoryRepository laboratoryRepository,
                               NotificationService notificationService,
                               com.catalog.repository.ApplicationFieldValueRepository applicationFieldValueRepository) {
            this.orderRepository = orderRepository;
            this.orderItemRepository = orderItemRepository;
            this.userRepository = userRepository;
            this.emailService = emailService;
            this.contractRepository = contractRepository;
            this.laboratoryRepository = laboratoryRepository;
            this.notificationService = notificationService;
            this.applicationFieldValueRepository = applicationFieldValueRepository;
        }

    @GetMapping
    public ResponseEntity<?> getAllOrders(@RequestParam(required = false) Integer labId) {
        try {
            List<Order> orders = labId != null
                ? orderRepository.findByLabId(labId)
                : orderRepository.findAll();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при получении заказов"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable int id) {
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) return ResponseEntity.status(404).body(errorResponse("Заказ не найден"));
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при получении заказа"));
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(@RequestParam int clientId) {
        try {
            return ResponseEntity.ok(orderRepository.findByClientId(clientId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при получении заказов клиента"));
        }
    }

    @GetMapping("/lab/{labId}")
    public ResponseEntity<?> getOrdersByLabId(@PathVariable int labId) {
        try {
            return ResponseEntity.ok(orderRepository.findByLabId(labId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при получении заказов лаборатории"));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        try {
            return ResponseEntity.ok(orderRepository.findByStatus(status));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при фильтрации заказов"));
        }
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<?> getOrderItems(@PathVariable int id) {
        try {
            return ResponseEntity.ok(orderItemRepository.findByOrderId(id));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при получении позиций заявки"));
        }
    }

    // POST /api/orders — создать заявку (totalPrice убран)
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            if (request.getClientId() == null)
                return ResponseEntity.status(400).body(errorResponse("ID клиента обязателен"));
            if (request.getServiceId() == null)
                return ResponseEntity.status(400).body(errorResponse("ID услуги обязателен"));
            if (request.getLabId() == null)
                return ResponseEntity.status(400).body(errorResponse("ID лаборатории обязателен"));
            if (request.getDueDate() == null || request.getDueDate().isEmpty())
                return ResponseEntity.status(400).body(errorResponse("Дата сдачи обязательна"));
            if (request.getOrderItems() == null || request.getOrderItems().isEmpty())
                return ResponseEntity.status(400).body(errorResponse("Добавьте хотя бы один прибор"));

            for (OrderItemRequest item : request.getOrderItems()) {
                if (item.getDeviceType() == null || item.getDeviceType().isEmpty())
                    return ResponseEntity.status(400).body(errorResponse("Тип прибора обязателен"));
                if (item.getSerialNumber() == null || item.getSerialNumber().isEmpty())
                    return ResponseEntity.status(400).body(errorResponse("Серийный номер обязателен"));
                if (item.getQuantity() == null || item.getQuantity() <= 0)
                    return ResponseEntity.status(400).body(errorResponse("Количество должно быть больше 0"));
            }

            Order order = new Order();
            order.setOrderNumber("ORD-" + System.currentTimeMillis());
            order.setClientId(request.getClientId());
            order.setServiceId(request.getServiceId());
            order.setLabId(request.getLabId());
            order.setStatus("pending_contract");
            // totalPrice убран — цена объявляется финансистом позже
            try {
                LocalDate parsedDate = LocalDate.parse(request.getDueDate());
                if (parsedDate.getYear() > 2099 || parsedDate.getYear() < 2000)
                    return ResponseEntity.status(400).body(errorResponse("Некорректная дата"));
                order.setDueDate(parsedDate);
            } catch (Exception e) {
                return ResponseEntity.status(400).body(errorResponse("Неверный формат даты. Используйте YYYY-MM-DD"));
            }
            if (request.getClientComment() != null) order.setClientComment(request.getClientComment());
            orderRepository.save(order);

            for (OrderItemRequest itemReq : request.getOrderItems()) {
                OrderItem item = new OrderItem();
                item.setOrderId(order.getId());
                item.setDeviceType(itemReq.getDeviceType());
                item.setModel(itemReq.getModel());
                item.setSerialNumber(itemReq.getSerialNumber());
                item.setQuantity(itemReq.getQuantity());
                orderItemRepository.save(item);
            }

            // Создаём черновик договора
            Contract contract = new Contract(order.getId(), "CNT-" + System.currentTimeMillis());
            contractRepository.save(contract);

            notificationService.notifyManagersNewOrder(order.getOrderNumber());

            return ResponseEntity.status(201).body(orderRepository.findById(order.getId()).orElse(null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(errorResponse("Ошибка при создании заказа: " + e.getMessage()));
        }
    }

    // PUT /api/orders/{id} — редактирование менеджером (totalPrice убран)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable int id, @RequestBody UpdateOrderRequest request) {
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) return ResponseEntity.status(404).body(errorResponse("Заказ не найден"));

            if (request.getServiceId() != null) order.setServiceId(request.getServiceId());
            if (request.getLabId() != null) order.setLabId(request.getLabId());
            if (request.getDueDate() != null && !request.getDueDate().isEmpty()) {
                try { order.setDueDate(LocalDate.parse(request.getDueDate())); }
                catch (Exception e) { return ResponseEntity.status(400).body(errorResponse("Неверный формат даты")); }
            }
            if (request.getClientComment() != null) order.setClientComment(request.getClientComment());

            orderRepository.save(order);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при редактировании заявки"));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable int id, @RequestBody UpdateStatusRequest request) {
        try {
            List<String> validStatuses = List.of(
                "pending_contract", "revision", "awaiting_approval", "awaiting_director",
                "awaiting_payment", "pending_delivery", "awaiting_delivery", "received_in_lab",
                "in_work", "under_review", "completed", "cancelled", "annulled", "terminated"
            );
            if (request.getStatus() == null || !validStatuses.contains(request.getStatus()))
                return ResponseEntity.status(400).body(errorResponse("Недопустимый статус: " + request.getStatus()));

            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) return ResponseEntity.status(404).body(errorResponse("Заказ не найден"));

            order.setStatus(request.getStatus());
            orderRepository.save(order);

            User client = userRepository.findById(order.getClientId()).orElse(null);
            if (client != null && client.getEmail() != null) {
                if ("completed".equals(request.getStatus())) {
                    emailService.sendOrderCompleted(client.getEmail(), client.getFullName(), order.getOrderNumber());
                    notificationService.notifyClientCompleted(client.getId(), order.getId(), order.getOrderNumber());
                } else {
                    emailService.sendStatusUpdate(client.getEmail(), client.getFullName(), order.getOrderNumber(), request.getStatus());
                    notificationService.notifyClientStatusChanged(client.getId(), order.getId(), order.getOrderNumber(), request.getStatus());
                }
            }
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при обновлении статуса"));
        }
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}/return")
    public ResponseEntity<?> returnToRevision(@PathVariable int id, @RequestBody ReturnOrderRequest request) {
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) return ResponseEntity.status(404).body(errorResponse("Заказ не найден"));
            if (!"pending_contract".equals(order.getStatus()))
                return ResponseEntity.status(400).body(errorResponse("Вернуть на доработку можно только заявку в статусе 'pending_contract'"));
            if (request.getComment() == null || request.getComment().isBlank())
                return ResponseEntity.status(400).body(errorResponse("Комментарий обязателен"));

            order.setStatus("revision");
            order.setManagerComment(request.getComment().trim());
            orderRepository.save(order);

            notificationService.notifyClientRevision(order.getClientId(), order.getId(), order.getOrderNumber());
            User client = userRepository.findById(order.getClientId()).orElse(null);
            if (client != null)
                emailService.sendStatusUpdate(client.getEmail(), client.getFullName(), order.getOrderNumber(), "revision");

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при возврате на доработку"));
        }
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PutMapping("/{id}/resubmit")
    public ResponseEntity<?> resubmitOrder(@PathVariable int id, @RequestBody ResubmitOrderRequest request) {
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) return ResponseEntity.status(404).body(errorResponse("Заказ не найден"));
            if (!"revision".equals(order.getStatus()))
                return ResponseEntity.status(400).body(errorResponse("Повторно отправить можно только заявку в статусе 'revision'"));
            if (request.getOrderItems() == null || request.getOrderItems().isEmpty())
                return ResponseEntity.status(400).body(errorResponse("Добавьте хотя бы один прибор"));

            if (request.getServiceId() != null) order.setServiceId(request.getServiceId());
            if (request.getLabId() != null) order.setLabId(request.getLabId());
            if (request.getDueDate() != null && !request.getDueDate().isEmpty()) {
                try { order.setDueDate(LocalDate.parse(request.getDueDate())); }
                catch (Exception e) { return ResponseEntity.status(400).body(errorResponse("Неверный формат даты")); }
            }
            if (request.getClientComment() != null) order.setClientComment(request.getClientComment());
            order.setManagerComment(null);
            order.setStatus("pending_contract");
            orderRepository.save(order);

            orderItemRepository.deleteAll(orderItemRepository.findByOrderId(id));
            for (OrderItemRequest itemReq : request.getOrderItems()) {
                OrderItem item = new OrderItem();
                item.setOrderId(order.getId());
                item.setDeviceType(itemReq.getDeviceType());
                item.setModel(itemReq.getModel());
                item.setSerialNumber(itemReq.getSerialNumber());
                item.setQuantity(itemReq.getQuantity());
                orderItemRepository.save(item);
            }

            notificationService.notifyManagersResubmit(order.getOrderNumber());
            return ResponseEntity.ok(orderRepository.findById(id).orElse(order));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(errorResponse("Ошибка при повторной отправке: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}/send-invoice")
    public ResponseEntity<?> sendInvoice(@PathVariable int id) {
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) return ResponseEntity.status(404).body(errorResponse("Заказ не найден"));
            if (!"awaiting_payment".equals(order.getStatus()))
                return ResponseEntity.status(400).body(errorResponse("Счёт можно отправить только для заявки в статусе 'awaiting_payment'"));

            order.setInvoiceSent(true);
            orderRepository.save(order);
            notificationService.notifyClientInvoiceSent(order.getClientId(), order.getId(), order.getOrderNumber());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при отправке счёта"));
        }
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PutMapping("/{id}/upload-receipt")
    public ResponseEntity<?> uploadReceipt(@PathVariable int id, @RequestBody UploadReceiptRequest request) {
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) return ResponseEntity.status(404).body(errorResponse("Заказ не найден"));
            if (!"awaiting_payment".equals(order.getStatus()))
                return ResponseEntity.status(400).body(errorResponse("Чек можно загрузить только для заявки в статусе 'awaiting_payment'"));
            if (order.getPrice() == null)
                return ResponseEntity.status(400).body(errorResponse("Финансист ещё не объявил цену. Дождитесь счёта."));
            if (!order.isInvoiceSent())
                return ResponseEntity.status(400).body(errorResponse("Менеджер ещё не отправил вам счёт."));
            if (request.getFileData() == null || request.getFileData().isBlank())
                return ResponseEntity.status(400).body(errorResponse("Файл чека обязателен"));
            if (request.getFileName() == null || request.getFileName().isBlank())
                return ResponseEntity.status(400).body(errorResponse("Имя файла обязательно"));
            if (request.getFileData().length() > 7_000_000)
                return ResponseEntity.status(400).body(errorResponse("Файл слишком большой. Максимум 5MB"));

            order.setPaymentReceipt(request.getFileData());
            order.setPaymentReceiptName(request.getFileName());
            order.setReceiptUploadedAt(LocalDateTime.now());
            orderRepository.save(order);

            notificationService.notifyFinanciersReceiptUploaded(order.getId(), order.getOrderNumber());

            Map<String, Object> result = new HashMap<>();
            result.put("id", order.getId());
            result.put("orderNumber", order.getOrderNumber());
            result.put("status", order.getStatus());
            result.put("paymentReceiptName", order.getPaymentReceiptName());
            result.put("receiptUploadedAt", order.getReceiptUploadedAt());
            result.put("receiptUploaded", true);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при загрузке чека: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyRole('FINANCIER', 'MANAGER')")
    @GetMapping("/{id}/receipt")
    public ResponseEntity<?> getReceipt(@PathVariable int id) {
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) return ResponseEntity.status(404).body(errorResponse("Заказ не найден"));
            if (order.getPaymentReceipt() == null)
                return ResponseEntity.status(404).body(errorResponse("Чек ещё не загружен"));

            Map<String, Object> result = new HashMap<>();
            result.put("fileData", order.getPaymentReceipt());
            result.put("fileName", order.getPaymentReceiptName());
            result.put("uploadedAt", order.getReceiptUploadedAt());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при получении чека"));
        }
    }


    // PUT /api/orders/{id}/set-price
    // Финансист объявляет цену — записывается в order.price, статус не меняется
    @PutMapping("/{id}/set-price")
    public ResponseEntity<?> setPrice(@PathVariable int id, @RequestBody SetPriceRequest request) {
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) return ResponseEntity.status(404).body(errorResponse("Заказ не найден"));
            if (!"awaiting_payment".equals(order.getStatus()))
                return ResponseEntity.status(400).body(errorResponse("Цену можно объявить только для заявки в статусе \'awaiting_payment\'"));
            if (request.getPrice() == null || request.getPrice() <= 0)
                return ResponseEntity.status(400).body(errorResponse("Цена должна быть больше 0"));

            order.setPrice(request.getPrice());
            orderRepository.save(order);

            notificationService.notifyManagerInvoiceReady(order.getId(), order.getOrderNumber());

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при объявлении цены"));
        }
    }

    // PUT /api/orders/{id}/payment — финансист подтверждает оплату и устанавливает цену
    @PutMapping("/{id}/payment")
    public ResponseEntity<?> confirmPayment(@PathVariable int id, @RequestBody PaymentRequest request) {
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) return ResponseEntity.status(404).body(errorResponse("Заказ не найден"));

            order.setStatus("pending_delivery");
            if (request.getComment() != null) order.setPaymentComment(request.getComment());
            if (request.getInvoiceAmount() != null) order.setPrice(request.getInvoiceAmount());

            orderRepository.save(order);
            notificationService.notifyManagerPaymentConfirmed(order.getId(), order.getOrderNumber());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при подтверждении оплаты"));
        }
    }


    // PUT /api/orders/{id}/notify-director
    // Менеджер уведомляет руководителя что договор подписан и деньги поступили
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}/notify-director")
    public ResponseEntity<?> notifyDirector(@PathVariable int id) {
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) return ResponseEntity.status(404).body(errorResponse("Заказ не найден"));
            if (!"pending_delivery".equals(order.getStatus()))
                return ResponseEntity.status(400).body(errorResponse("Уведомить руководителя можно только для заявки в статусе 'pending_delivery'"));

            order.setStatus("awaiting_delivery");
            orderRepository.save(order);
            notificationService.notifyDirectorToAssign(order.getId(), order.getOrderNumber());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при отправке уведомления"));
        }
    }

    // PUT /api/orders/{id}/assign-lab — директор/ген.директор направляет в лабораторию
    @PreAuthorize("hasAnyRole('DIRECTOR', 'GEN_DIRECTOR')")
    @PutMapping("/{id}/assign-lab")
    public ResponseEntity<?> assignToLab(@PathVariable int id, @RequestBody AssignLabRequest request) {
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) return ResponseEntity.status(404).body(errorResponse("Заказ не найден"));
            if (!"awaiting_delivery".equals(order.getStatus()))
                return ResponseEntity.status(400).body(errorResponse("Направить можно только заявку в статусе 'awaiting_delivery'"));
            if (request.getLabId() == null)
                return ResponseEntity.status(400).body(errorResponse("ID лаборатории обязателен"));

            Laboratory lab = laboratoryRepository.findById(request.getLabId()).orElse(null);
            if (lab == null) return ResponseEntity.status(404).body(errorResponse("Лаборатория не найдена"));

            order.setAssignedLabId(request.getLabId());
            order.setAssignedAt(LocalDateTime.now());
            order.setStatus("received_in_lab");
            orderRepository.save(order);

            String labName = lab.getName() + (lab.getCity() != null ? " (" + lab.getCity() + ")" : "");
            notificationService.notifyAssignedToLab(order.getClientId(), order.getId(), order.getOrderNumber(), labName);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при направлении: " + e.getMessage()));
        }
    }

// GET /api/orders/{id}/fields
    // Returns all field values for an order
    @GetMapping("/{id}/fields")
    public ResponseEntity<?> getFieldValues(@PathVariable int id) {
        try {
            return ResponseEntity.ok(
                applicationFieldValueRepository.findByOrderId(id)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при получении полей"));
        }
    }

    // POST /api/orders/{id}/fields
    // Client saves field values
    @PostMapping("/{id}/fields")
    public ResponseEntity<?> saveFieldValues(
            @PathVariable int id,
            @RequestBody List<FieldValueRequest> fields) {
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) return ResponseEntity.status(404).body(errorResponse("Заказ не найден"));

            applicationFieldValueRepository.deleteByOrderId(id);

            for (FieldValueRequest fvr : fields) {
                com.catalog.models.ApplicationFieldValue afv = new com.catalog.models.ApplicationFieldValue();
                afv.setOrderId(id);
                afv.setFieldKey(fvr.fieldKey);
                afv.setFieldValue(fvr.fieldValue);
                afv.setRowIndex(fvr.rowIndex != null ? fvr.rowIndex : 0);
                afv.setFilledByRole(fvr.filledByRole != null ? fvr.filledByRole : "client");
                afv.setUpdatedAt(LocalDateTime.now());
                applicationFieldValueRepository.save(afv);
            }

            return ResponseEntity.ok(applicationFieldValueRepository.findByOrderId(id));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при сохранении полей"));
        }
    }

    // PUT /api/orders/{id}/toggle-client-edit
    // Бухгалтер toggles client edit access
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}/toggle-client-edit")
    public ResponseEntity<?> toggleClientEdit(@PathVariable int id) {
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) return ResponseEntity.status(404).body(errorResponse("Заказ не найден"));

            order.setClientEditEnabled(!order.isClientEditEnabled());
            orderRepository.save(order);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при переключении доступа"));
        }
    }

    private Map<String, String> errorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }


    public static class CreateOrderRequest {
        public Integer clientId, serviceId, labId;
        public String dueDate, clientComment;
        public List<OrderItemRequest> orderItems;
        public Integer getClientId() { return clientId; }
        public Integer getServiceId() { return serviceId; }
        public Integer getLabId() { return labId; }
        public String getDueDate() { return dueDate; }
        public String getClientComment() { return clientComment; }
        public List<OrderItemRequest> getOrderItems() { return orderItems; }
    }

    public static class UpdateOrderRequest {
        public Integer serviceId, labId;
        public String dueDate, clientComment;
        public Integer getServiceId() { return serviceId; }
        public Integer getLabId() { return labId; }
        public String getDueDate() { return dueDate; }
        public String getClientComment() { return clientComment; }
    }

    public static class ResubmitOrderRequest {
        public Integer serviceId, labId;
        public String dueDate, clientComment;
        public List<OrderItemRequest> orderItems;
        public Integer getServiceId() { return serviceId; }
        public Integer getLabId() { return labId; }
        public String getDueDate() { return dueDate; }
        public String getClientComment() { return clientComment; }
        public List<OrderItemRequest> getOrderItems() { return orderItems; }
    }

    public static class OrderItemRequest {
        public String deviceType, model, serialNumber;
        public Integer quantity;
        // unitPrice убран — цена устанавливается финансистом
        public String getDeviceType() { return deviceType; }
        public String getModel() { return model; }
        public String getSerialNumber() { return serialNumber; }
        public Integer getQuantity() { return quantity; }
    }

    public static class UpdateStatusRequest {
        public String status;
        public String getStatus() { return status; }
    }

    public static class ReturnOrderRequest {
        public String comment;
        public String getComment() { return comment; }
    }

    public static class AssignLabRequest {
        public Integer labId;
        public Integer getLabId() { return labId; }
    }

    public static class UploadReceiptRequest {
        public String fileData, fileName;
        public String getFileData() { return fileData; }
        public String getFileName() { return fileName; }
    }


    public static class SetPriceRequest {
        public Double price;
        public Double getPrice() { return price; }
    }

    public static class PaymentRequest {
        public String comment;
        public boolean paid;
        public Double price;
        public String getComment() { return comment; }
        public boolean isPaid() { return paid; }
        public Double getInvoiceAmount() { return price; }
    }

    public static class FieldValueRequest {
        public String fieldKey, fieldValue, filledByRole;
        public Integer rowIndex;
    }



}