package com.catalog.controllers;

import com.catalog.models.Subservice;
import com.catalog.models.SubserviceField;
import com.catalog.repository.SubserviceFieldRepository;
import com.catalog.repository.SubserviceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subservices")
public class SubserviceController {

    private final SubserviceRepository subserviceRepository;
    private final SubserviceFieldRepository subserviceFieldRepository;

private final com.catalog.repository.ServiceRepository serviceRepository;

    public SubserviceController(SubserviceRepository subserviceRepository,
                                SubserviceFieldRepository subserviceFieldRepository,
                                com.catalog.repository.ServiceRepository serviceRepository) {
        this.subserviceRepository = subserviceRepository;
        this.subserviceFieldRepository = subserviceFieldRepository;
        this.serviceRepository = serviceRepository;
    }

    // GET /api/subservices?serviceId=1
    // Returns all active subservices for a given service
    @GetMapping
    public ResponseEntity<List<Subservice>> getByService(@RequestParam int serviceId) {
        List<Subservice> subservices = subserviceRepository
                .findByServiceIdAndIsActive(serviceId, true);

        com.catalog.models.Service svc = serviceRepository
                .findById(serviceId).orElse(null);
        if (svc != null && svc.getCode() != null) {
            subservices.forEach(s ->
                s.setFullCode(svc.getCode() + "_" + s.getCode())
            );
        }
        return ResponseEntity.ok(subservices);
    }

    // GET /api/subservices/{id}
    // Returns a single subservice by id
    @GetMapping("/{id}")
    public ResponseEntity<Subservice> getById(@PathVariable int id) {
        return subserviceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/subservices/{id}/fields
    // Returns the form field schema for a subservice
    @GetMapping("/{id}/fields")
    public ResponseEntity<List<SubserviceField>> getFields(@PathVariable int id) {
        List<SubserviceField> fields = subserviceFieldRepository
                .findBySubserviceIdOrderBySortOrder(id);
        return ResponseEntity.ok(fields);
    }
}