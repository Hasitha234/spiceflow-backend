package com.spiceflow.backend.admin.service;

import com.spiceflow.backend.admin.dto.request.BusinessTypeRequest;
import com.spiceflow.backend.admin.dto.response.BusinessTypeResponse;
import com.spiceflow.backend.admin.entity.BusinessType;
import com.spiceflow.backend.admin.repository.BusinessTypeRepository;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessTypeService {

    private final BusinessTypeRepository businessTypeRepository;
    private final TenantRepository tenantRepository;

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "businessTypes", allEntries = true)
    public BusinessTypeResponse createBusinessType(BusinessTypeRequest request) {
        log.info("Creating new business type: {}", request.getName());
        
        if (businessTypeRepository.existsByName(request.getName())) {
            throw new BusinessRuleViolationException("Business type with name '" + request.getName() + "' already exists");
        }

        BusinessType businessType = BusinessType.builder()
            .name(request.getName())
            .description(request.getDescription())
            .build();

        businessType = businessTypeRepository.save(businessType);
        return mapToResponse(businessType);
    }

    @Cacheable(value = "businessTypes", keyGenerator = "simpleKeyGenerator")
    public BusinessTypeResponse getBusinessType(Long id) {
        BusinessType businessType = getBusinessTypeEntity(id);
        return mapToResponse(businessType);
    }

    @Cacheable(value = "businessTypes", keyGenerator = "simpleKeyGenerator")
    public List<BusinessTypeResponse> getAllBusinessTypes() {
        return businessTypeRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "businessTypes", allEntries = true)
    public BusinessTypeResponse updateBusinessType(Long id, BusinessTypeRequest request) {
        log.info("Updating business type with ID: {}", id);
        
        BusinessType businessType = getBusinessTypeEntity(id);
        
        if (!businessType.getName().equals(request.getName()) && businessTypeRepository.existsByName(request.getName())) {
            throw new BusinessRuleViolationException("Business type with name '" + request.getName() + "' already exists");
        }

        businessType.setName(request.getName());
        businessType.setDescription(request.getDescription());

        businessType = businessTypeRepository.save(businessType);
        return mapToResponse(businessType);
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "businessTypes", allEntries = true)
    public void deleteBusinessType(Long id) {
        log.info("Deleting business type with ID: {}", id);
        
        BusinessType businessType = getBusinessTypeEntity(id);
        
        // Check if any tenants are using this business type
        long tenantsCount = tenantRepository.countByBusinessTypeId(id);
        if (tenantsCount > 0) {
            throw new BusinessRuleViolationException("Cannot delete business type because " + tenantsCount + " tenant(s) are using it");
        }

        businessTypeRepository.delete(businessType);
    }

    public BusinessType getBusinessTypeEntity(Long id) {
        return businessTypeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Business Type not found with id: " + id));
    }

    private BusinessTypeResponse mapToResponse(BusinessType entity) {
        return BusinessTypeResponse.builder()
            .id(entity.getId())
            .name(entity.getName())
            .description(entity.getDescription())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
