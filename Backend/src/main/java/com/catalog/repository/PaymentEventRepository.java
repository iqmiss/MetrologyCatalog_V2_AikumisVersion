package com.catalog.repository;

import com.catalog.models.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentEventRepository extends JpaRepository<PaymentEvent, Integer> {
    List<PaymentEvent> findByOrderId(int orderId);
    List<PaymentEvent> findBySentTo1c(boolean sentTo1c);
}