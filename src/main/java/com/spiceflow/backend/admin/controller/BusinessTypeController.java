package com.spiceflow.backend.admin.controller;

import org.springframework.validation.annotation.Validated;
import com.spiceflow.backend.admin.dto.request.BusinessTypeRequest;
import com.spiceflow.backend.admin.dto.response.BusinessTypeResponse;
import com.spiceflow.backend.admin.service.BusinessTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/v1/admin/business-types")
@RequiredArgsConstructor
@Tag(name = "1. Super Admin Operations")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class BusinessTypeController {

    private final BusinessTypeService businessTypeService;

    @PostMapping
    @Operation(summary = "Create a new business type", description = "Provision a new business type for tenants")
    public ResponseEntity<BusinessTypeResponse> createBusinessType(@Valid @RequestBody BusinessTypeRequest request) {
        BusinessTypeResponse response = businessTypeService.createBusinessType(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a business type by ID", description = "Fetch details of a single business type")
    public ResponseEntity<BusinessTypeResponse> getBusinessType(@PathVariable Long id) {
        BusinessTypeResponse response = businessTypeService.getBusinessType(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List all business types", description = "Fetch all available business types")
    public ResponseEntity<List<BusinessTypeResponse>> getAllBusinessTypes() {
        List<BusinessTypeResponse> responses = businessTypeService.getAllBusinessTypes();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a business type", description = "Update the name or description of a business type")
    public ResponseEntity<BusinessTypeResponse> updateBusinessType(@PathVariable Long id, @Valid @RequestBody BusinessTypeRequest request) {
        BusinessTypeResponse response = businessTypeService.updateBusinessType(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a business type", description = "Delete a business type if no tenants are using it")
    public ResponseEntity<Void> deleteBusinessType(@PathVariable Long id) {
        businessTypeService.deleteBusinessType(id);
        return ResponseEntity.noContent().build();
    }
}


