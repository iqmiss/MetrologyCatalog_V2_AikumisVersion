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
// Обрабатывает все операции с заявками: создание, получение, смена статуса
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    // Репозитории для работы с таблицами БД
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;

    // Сервис для отправки email уведомлений
    private final EmailService emailService;

    // Spring автоматически передаёт все зависимости через конструктор (Dependency Injection)
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

    // GET /api/orders
    // Возвращает все заявки — используется метрологом и менеджером в Queue
    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        try {
            List<Order> orders = orderRepository.findAll();
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
    // Возвращает заявки конкретного клиента — используется в MyOrders
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
    // Создаёт новую заявку, сохраняет приборы и автоматически генерирует договор
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            // Валидация обязательных полей заявки
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

            // Валидация каждого прибора в заявке
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

            // Создаём объект заявки и сохраняем в БД
            // После save() объект order получает сгенерированный id из БД
            Order order = new Order();
            order.setOrderNumber("ORD-" + System.currentTimeMillis());
            order.setClientId(request.getClientId());
            order.setServiceId(request.getServiceId());
            order.setLabId(request.getLabId());
            order.setStatus("new");
            order.setTotalPrice(request.getTotalPrice());
            order.setDueDate(LocalDate.parse(request.getDueDate()));
            orderRepository.save(order);

            // Сохраняем приборы привязанные к заявке (таблица order_items)
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
            contract.setSigned(false);
            contractRepository.save(contract);

            // Возвращаем созданную заявку из БД
            Order createdOrder = orderRepository.findById(order.getId()).orElse(null);
            return ResponseEntity.status(201).body(createdOrder);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(errorResponse("Ошибка при создании заказа: " + e.getMessage()));
        }
    }

    // PUT /api/orders/{id}/status
    // Обновляет статус заявки и отправляет email уведомление клиенту
    // Используется метрологом в Queue при нажатии "Далее →"
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable int id,
            @RequestBody UpdateStatusRequest request
    ) {
        try {
            // Валидация — проверяем что переданный статус допустимый
            List<String> validStatuses = List.of(
                "new", "awaiting_payment", "awaiting_delivery",
                "received_in_lab", "in_work", "under_review", "completed"
            );
            if (request.getStatus() == null || !validStatuses.contains(request.getStatus())) {
                return ResponseEntity.status(400).body(errorResponse("Недопустимый статус: " + request.getStatus()));
            }

            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) {
                return ResponseEntity.status(404).body(errorResponse("Заказ не найден"));
            }

            // Обновляем статус в БД
            order.setStatus(request.getStatus());
            orderRepository.save(order);

            // Отправляем email уведомление клиенту об изменении статуса
            User client = userRepository.findById(order.getClientId()).orElse(null);
            if (client != null && client.getEmail() != null) {
                if ("completed".equals(request.getStatus())) {
                    // Специальное письмо когда заявка завершена
                    emailService.sendOrderCompleted(
                        client.getEmail(),
                        client.getFullName(),
                        order.getOrderNumber()
                    );
                } else {
                    // Стандартное письмо об изменении статуса
                    emailService.sendStatusUpdate(
                        client.getEmail(),
                        client.getFullName(),
                        order.getOrderNumber(),
                        request.getStatus()
                    );
                }
            }

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorResponse("Ошибка при обновлении статуса"));
        }
    }

    // Вспомогательный метод для формирования ответа с ошибкой
    private Map<String, String> errorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }

    // Класс для десериализации тела запроса при создании заявки
    public static class CreateOrderRequest {
        public Integer clientId;  // ID клиента который подаёт заявку
        public Integer serviceId; // ID выбранной услуги из каталога
        public Integer labId;     // ID выбранной лаборатории
        public Double totalPrice; // Итоговая стоимость (цена × количество)
        public String dueDate;    // Плановая дата сдачи прибора
        public List<OrderItemRequest> orderItems; // Список приборов

        public Integer getClientId() { return clientId; }
        public Integer getServiceId() { return serviceId; }
        public Integer getLabId() { return labId; }
        public Double getTotalPrice() { return totalPrice; }
        public String getDueDate() { return dueDate; }
        public List<OrderItemRequest> getOrderItems() { return orderItems; }
    }

    // Класс для десериализации данных одного прибора в заявке
    public static class OrderItemRequest {
        public String deviceType;   // Тип прибора (Манометр, Термометр и т.д.)
        public String model;        // Модель прибора
        public String serialNumber; // Заводской серийный номер
        public Integer quantity;    // Количество единиц
        public Double unitPrice;    // Цена за единицу

        public String getDeviceType() { return deviceType; }
        public String getModel() { return model; }
        public String getSerialNumber() { return serialNumber; }
        public Integer getQuantity() { return quantity; }
        public Double getUnitPrice() { return unitPrice; }
    }

    // Класс для десериализации запроса на смену статуса
    public static class UpdateStatusRequest {
        public String status; // Новый статус заявки
        public String getStatus() { return status; }
    }
}