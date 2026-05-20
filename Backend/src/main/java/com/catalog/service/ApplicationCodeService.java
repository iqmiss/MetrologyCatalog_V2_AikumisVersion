package com.catalog.service;

import com.catalog.models.ApplicationCounter;
import com.catalog.models.Order;
import com.catalog.models.Subservice;
import com.catalog.repository.ApplicationCounterRepository;
import com.catalog.repository.ServiceRepository;
import com.catalog.repository.SubserviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationCodeService {

    private final ApplicationCounterRepository counterRepository;
    private final SubserviceRepository subserviceRepository;
    private final ServiceRepository serviceRepository;

    public ApplicationCodeService(ApplicationCounterRepository counterRepository,
                                   SubserviceRepository subserviceRepository,
                                   ServiceRepository serviceRepository) {
        this.counterRepository = counterRepository;
        this.subserviceRepository = subserviceRepository;
        this.serviceRepository = serviceRepository;
    }
    @Transactional
    public String generateCode(Order order) {
        if (order.getSubserviceId() == null) {
            return "ORD-" + order.getId();
        }

        Subservice subservice = subserviceRepository
                .findById(order.getSubserviceId()).orElse(null);
        if (subservice == null) return "ORD-" + order.getId();

        com.catalog.models.Service svc = serviceRepository
                .findById(subservice.getServiceId()).orElse(null);
        if (svc == null || svc.getCode() == null) return "ORD-" + order.getId();

        String serviceCode = svc.getCode();
        String subserviceCode = subservice.getCode();

        ApplicationCounter counter = counterRepository
                .findByCodesForUpdate(serviceCode, subserviceCode)
                .orElseGet(() -> {
                    ApplicationCounter newCounter = new ApplicationCounter();
                    newCounter.setServiceCode(serviceCode);
                    newCounter.setSubserviceCode(subserviceCode);
                    newCounter.setLastNumber(0);
                    return counterRepository.save(newCounter);
                });

        counter.setLastNumber(counter.getLastNumber() + 1);
        counterRepository.save(counter);

        String paddedNumber = String.format("%04d", counter.getLastNumber());
        return serviceCode + "_" + subserviceCode + "_" + paddedNumber;
    }
}