package com.catalog.controllers;

import com.catalog.models.OrderDemand;
import com.catalog.repository.OrderDemandRepository;
import com.catalog.repository.OrderRepository;
import com.catalog.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demands")
public class DemandController {

    private final OrderDemandRepository orderDemandRepository;
    private final OrderRepository orderRepository;
    private final JwtUtil jwtUtil;

    public DemandController(OrderDemandRepository orderDemandRepository,
                            OrderRepository orderRepository,
                            JwtUtil jwtUtil) {
        this.orderDemandRepository = orderDemandRepository;
        this.orderRepository = orderRepository;
        this.jwtUtil = jwtUtil;
    }

    // GET /api/demands/{orderId}
    // Returns all demands for an order
    @GetMapping("/{orderId}")
    public ResponseEntity<List<OrderDemand>> getDemands(@PathVariable int orderId) {
        return ResponseEntity.ok(
            orderDemandRepository.findByOrderIdOrderByCreatedAt(orderId)
        );
    }

    // POST /api/demands/{orderId}
    // Metrologist creates a demand
    @PostMapping("/{orderId}")
    public ResponseEntity<OrderDemand> createDemand(
            @PathVariable int orderId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {

        String token = authHeader.replace("Bearer ", "");
        int createdBy = jwtUtil.getUserId(token);

        OrderDemand demand = new OrderDemand();
        demand.setOrderId(orderId);
        demand.setCreatedBy(createdBy);
        demand.setDemandText(body.get("demandText"));
        demand.setStatus("open");
        demand.setCreatedAt(LocalDateTime.now());

        // Set secondary status on the order
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setSecondaryStatus("awaiting_client_response");
            orderRepository.save(order);
        });

        OrderDemand saved = orderDemandRepository.save(demand);
        return ResponseEntity.status(201).body(saved);
    }

    // PUT /api/demands/{id}/fulfill
    // Metrologist marks a demand as fulfilled
    @PutMapping("/{id}/fulfill")
    public ResponseEntity<OrderDemand> fulfillDemand(
            @PathVariable int id,
            @RequestHeader("Authorization") String authHeader) {

        return orderDemandRepository.findById(id)
                .map(demand -> {
                    demand.setStatus("fulfilled");
                    demand.setFulfilledAt(LocalDateTime.now());

                    // Check if all demands for this order are fulfilled
                    int openCount = orderDemandRepository
                            .countByOrderIdAndStatus(demand.getOrderId(), "open");

                    if (openCount <= 1) {
                        // This was the last open demand — clear secondary status
                        orderRepository.findById(demand.getOrderId())
                                .ifPresent(order -> {
                                    order.setSecondaryStatus(null);
                                    orderRepository.save(order);
                                });
                    }

                    return ResponseEntity.ok(orderDemandRepository.save(demand));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}