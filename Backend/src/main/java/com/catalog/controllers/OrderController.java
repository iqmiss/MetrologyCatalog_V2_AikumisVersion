package com.catalog.controllers;

import com.catalog.models.User;
import com.catalog.models.Order;
import com.catalog.models.Contract;
import com.catalog.models.OrderItem;
import com.catalog.repository.UserRepository;
import com.catalog.repository.OrderRepository;
import com.catalog.repository.ContractRepository;
import com.catalog.repository.OrderItemRepository;
import com.catalog.service.EmailService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Контроллер для управления заявками
// Обрабатывает все операции с заявками: создание, получение, смена статуса, редактирование
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final EmailService emailService;

    public OrderController(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        UserRepository userRepository,
                        EmailService emailService,
                        ContractRepository contractRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.contractRepository = contractRepository;
    }

    // GET /api/orders?labId=1
    // Возвращает все заявки — используется метрологом и менеджером
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

    // GET /api/orders/{id}
    // Возвращает одну заявку по ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable int id) {
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) {
                return ResponseEntity.status(404).body(errorResponse("Заказ не найден"));
            }
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при получении заказа"));
        }
    }

    // GET /api/orders/my-orders?clientId=1
    // Возвращает заявки конкретного клиента
    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(@RequestParam int clientId) {
        try {
            List<Order> orders = orderRepository.findByClientId(clientId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при получении заказов клиента"));
        }
    }

    // GET /api/orders/lab/{labId}
    // Возвращает заявки конкретной лаборатории
    @GetMapping("/lab/{labId}")
    public ResponseEntity<?> getOrdersByLabId(@PathVariable int labId) {
        try {
            List<Order> orders = orderRepository.findByLabId(labId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при получении заказов лаборатории"));
        }
    }

    // GET /api/orders/status/{status}
    // Фильтрует заявки по статусу
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        try {
            List<Order> orders = orderRepository.findByStatus(status);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при фильтрации заказов"));
        }
    }

    // POST /api/orders
    // Создаёт новую заявку — доступно клиенту и менеджеру
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            if (request.getClientId() == null) {
                return ResponseEntity.status(400).body(errorResponse("ID клиента обязателен"));
            }
            if (request.getServiceId() == null) {
                return ResponseEntity.status(400).body(errorResponse("ID услуги обязателен"));
            }
            if (request.getLabId() == null) {
                return ResponseEntity.status(400).body(errorResponse("ID лаборатории обязателен"));
            }
            if (request.getTotalPrice() == null || request.getTotalPrice() <= 0) {
                return ResponseEntity.status(400).body(errorResponse("Стоимость должна быть больше 0"));
            }
            if (request.getDueDate() == null || request.getDueDate().isEmpty()) {
                return ResponseEntity.status(400).body(errorResponse("Дата сдачи обязательна"));
            }
            if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
                return ResponseEntity.status(400).body(errorResponse("Добавьте хотя бы один прибор"));
            }

            for (OrderItemRequest item : request.getOrderItems()) {
                if (item.getDeviceType() == null || item.getDeviceType().isEmpty()) {
                    return ResponseEntity.status(400).body(errorResponse("Тип прибора обязателен"));
                }
                if (item.getSerialNumber() == null || item.getSerialNumber().isEmpty()) {
                    return ResponseEntity.status(400).body(errorResponse("Серийный номер обязателен"));
                }
                if (item.getQuantity() == null || item.getQuantity() <= 0) {
                    return ResponseEntity.status(400).body(errorResponse("Количество должно быть больше 0"));
                }
            }

            Order order = new Order();
            order.setOrderNumber("ORD-" + System.currentTimeMillis());
            order.setClientId(request.getClientId());
            order.setServiceId(request.getServiceId());
            order.setLabId(request.getLabId());
            order.setStatus("pending_contract");
            order.setTotalPrice(request.getTotalPrice());
            order.setDueDate(LocalDate.parse(request.getDueDate()));
            if (request.getClientComment() != null) {
                order.setClientComment(request.getClientComment());
            }
            orderRepository.save(order);

            for (OrderItemRequest itemReq : request.getOrderItems()) {
                OrderItem item = new OrderItem();
                item.setOrderId(order.getId());
                item.setDeviceType(itemReq.getDeviceType());
                item.setModel(itemReq.getModel());
                item.setSerialNumber(itemReq.getSerialNumber());
                item.setQuantity(itemReq.getQuantity());
                item.setUnitPrice(itemReq.getUnitPrice());
                orderItemRepository.save(item);
            }

            // Автоматически создаём договор сразу после подачи заявки
            Contract contract = new Contract();
            contract.setOrderId(order.getId());
            contract.setContractNumber("CNT-" + System.currentTimeMillis());
            contract.setClientSigned(false);
            contractRepository.save(contract);

            Order createdOrder = orderRepository.findById(order.getId()).orElse(null);
            return ResponseEntity.status(201).body(createdOrder);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(errorResponse("Ошибка при создании заказа: " + e.getMessage()));
        }
    }

    // PUT /api/orders/{id}
    // Менеджер редактирует заявку — услугу, лабораторию, дату, стоимость, комментарий
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable int id, @RequestBody UpdateOrderRequest request) {
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) {
                return ResponseEntity.status(404).body(errorResponse("Заказ не найден"));
            }

            if (request.getServiceId() != null) order.setServiceId(request.getServiceId());
            if (request.getLabId() != null) order.setLabId(request.getLabId());
            if (request.getTotalPrice() != null) order.setTotalPrice(request.getTotalPrice());
            if (request.getDueDate() != null && !request.getDueDate().isEmpty()) {
                order.setDueDate(LocalDate.parse(request.getDueDate()));
            }
            if (request.getClientComment() != null) order.setClientComment(request.getClientComment());

            orderRepository.save(order);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при редактировании заявки"));
        }
    }

    // PUT /api/orders/{id}/status
    // Обновляет статус заявки и отправляет email уведомление клиенту
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable int id,
            @RequestBody UpdateStatusRequest request) {
        try {
            List<String> validStatuses = List.of(
                "pending_contract", "awaiting_approval", "awaiting_director",
                "awaiting_payment", "awaiting_delivery", "received_in_lab",
                "in_work", "under_review", "completed", "cancelled",
                "annulled", "terminated"
            );
            if (request.getStatus() == null || !validStatuses.contains(request.getStatus())) {
                return ResponseEntity.status(400).body(errorResponse("Недопустимый статус: " + request.getStatus()));
            }

            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) {
                return ResponseEntity.status(404).body(errorResponse("Заказ не найден"));
            }

            // Блокируем переход в received_in_lab если договор не подписан
            if ("received_in_lab".equals(request.getStatus())) {
                Contract contract = contractRepository.findByOrderId(id).orElse(null);
                if (contract == null || !contract.isClientSigned() || !contract.isDirectorSigned()) {
                    return ResponseEntity.status(400).body(
                        errorResponse("Договор должен быть подписан клиентом и директором")
                    );
                }
            }

            order.setStatus(request.getStatus());
            orderRepository.save(order);

            // Email уведомление клиенту
            User client = userRepository.findById(order.getClientId()).orElse(null);
            if (client != null && client.getEmail() != null) {
                if ("completed".equals(request.getStatus())) {
                    emailService.sendOrderCompleted(
                        client.getEmail(), client.getFullName(), order.getOrderNumber());
                } else {
                    emailService.sendStatusUpdate(
                        client.getEmail(), client.getFullName(), order.getOrderNumber(), request.getStatus());
                }
            }

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при обновлении статуса"));
        }
    }

    // PUT /api/orders/{id}/payment
    // Финансист подтверждает оплату или пропускает
    @PutMapping("/{id}/payment")
    public ResponseEntity<?> confirmPayment(@PathVariable int id, @RequestBody PaymentRequest request) {
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) {
                return ResponseEntity.status(404).body(errorResponse("Заказ не найден"));
            }

            order.setStatus("awaiting_delivery");
            if (request.getComment() != null) order.setPaymentComment(request.getComment());
            if (request.getInvoiceAmount() != null) order.setInvoiceAmount(request.getInvoiceAmount());

            orderRepository.save(order);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при подтверждении оплаты"));
        }
    }

    private Map<String, String> errorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }

    public static class CreateOrderRequest {
        public Integer clientId;
        public Integer serviceId;
        public Integer labId;
        public Double totalPrice;
        public String dueDate;
        public List<OrderItemRequest> orderItems;
        public String clientComment;

        public Integer getClientId() { return clientId; }
        public Integer getServiceId() { return serviceId; }
        public Integer getLabId() { return labId; }
        public Double getTotalPrice() { return totalPrice; }
        public String getDueDate() { return dueDate; }
        public List<OrderItemRequest> getOrderItems() { return orderItems; }
        public String getClientComment() { return clientComment; }
    }

    public static class UpdateOrderRequest {
        public Integer serviceId;
        public Integer labId;
        public Double totalPrice;
        public String dueDate;
        public String clientComment;

        public Integer getServiceId() { return serviceId; }
        public Integer getLabId() { return labId; }
        public Double getTotalPrice() { return totalPrice; }
        public String getDueDate() { return dueDate; }
        public String getClientComment() { return clientComment; }
    }

    public static class OrderItemRequest {
        public String deviceType;
        public String model;
        public String serialNumber;
        public Integer quantity;
        public Double unitPrice;

        public String getDeviceType() { return deviceType; }
        public String getModel() { return model; }
        public String getSerialNumber() { return serialNumber; }
        public Integer getQuantity() { return quantity; }
        public Double getUnitPrice() { return unitPrice; }
    }

    public static class UpdateStatusRequest {
        public String status;
        public String getStatus() { return status; }
    }

    public static class PaymentRequest {
        public String comment;
        public boolean paid;
        public Double invoiceAmount;
        public String getComment() { return comment; }
        public boolean isPaid() { return paid; }
        public Double getInvoiceAmount() { return invoiceAmount; }
    }
}