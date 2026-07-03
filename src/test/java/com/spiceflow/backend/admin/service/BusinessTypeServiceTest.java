package com.spiceflow.backend.admin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spiceflow.backend.admin.dto.request.BusinessTypeRequest;
import com.spiceflow.backend.admin.dto.response.BusinessTypeResponse;
import com.spiceflow.backend.admin.entity.BusinessType;
import com.spiceflow.backend.admin.repository.BusinessTypeRepository;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessTypeServiceTest {

    @Mock private BusinessTypeRepository businessTypeRepository;
    @Mock private TenantRepository tenantRepository;

    @InjectMocks private BusinessTypeService businessTypeService;

    private BusinessType businessType;

    @BeforeEach
    void setUp() {
        businessType = new BusinessType();
        businessType.setId(1L);
        businessType.setName("Retail");
        businessType.setDescription("Retail description");
    }

    @Test
    void createBusinessType_Success() {
        BusinessTypeRequest request = new BusinessTypeRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "name", "New Retail");
        org.springframework.test.util.ReflectionTestUtils.setField(request, "description", "Description");
        when(businessTypeRepository.existsByName(request.getName())).thenReturn(false);
        when(businessTypeRepository.save(any(BusinessType.class))).thenReturn(businessType);

        BusinessTypeResponse response = businessTypeService.createBusinessType(request);

        assertNotNull(response);
        assertEquals(businessType.getName(), response.getName());
        verify(businessTypeRepository).save(any(BusinessType.class));
    }

    @Test
    void createBusinessType_NameExists() {
        BusinessTypeRequest request = new BusinessTypeRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "name", "Retail");
        org.springframework.test.util.ReflectionTestUtils.setField(request, "description", "Description");
        when(businessTypeRepository.existsByName(request.getName())).thenReturn(true);

        assertThrows(BusinessRuleViolationException.class, () -> businessTypeService.createBusinessType(request));
    }

    @Test
    void getBusinessType_Success() {
        when(businessTypeRepository.findById(1L)).thenReturn(Optional.of(businessType));

        BusinessTypeResponse response = businessTypeService.getBusinessType(1L);

        assertNotNull(response);
        assertEquals(businessType.getName(), response.getName());
    }

    @Test
    void getBusinessType_NotFound() {
        when(businessTypeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> businessTypeService.getBusinessType(1L));
    }

    @Test
    void getAllBusinessTypes_Success() {
        when(businessTypeRepository.findAll()).thenReturn(List.of(businessType));

        List<BusinessTypeResponse> responses = businessTypeService.getAllBusinessTypes();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(businessType.getName(), responses.get(0).getName());
    }

    @Test
    void updateBusinessType_Success() {
        BusinessTypeRequest request = new BusinessTypeRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "name", "Updated Retail");
        org.springframework.test.util.ReflectionTestUtils.setField(request, "description", "New Description");
        when(businessTypeRepository.findById(1L)).thenReturn(Optional.of(businessType));
        when(businessTypeRepository.existsByName(request.getName())).thenReturn(false);
        when(businessTypeRepository.save(any(BusinessType.class))).thenReturn(businessType);

        BusinessTypeResponse response = businessTypeService.updateBusinessType(1L, request);

        assertNotNull(response);
        verify(businessTypeRepository).save(businessType);
    }

    @Test
    void updateBusinessType_NameExists() {
        BusinessTypeRequest request = new BusinessTypeRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "name", "Existing Retail");
        org.springframework.test.util.ReflectionTestUtils.setField(request, "description", "New Description");
        when(businessTypeRepository.findById(1L)).thenReturn(Optional.of(businessType));
        when(businessTypeRepository.existsByName(request.getName())).thenReturn(true);

        assertThrows(BusinessRuleViolationException.class, () -> businessTypeService.updateBusinessType(1L, request));
    }

    @Test
    void deleteBusinessType_Success() {
        when(businessTypeRepository.findById(1L)).thenReturn(Optional.of(businessType));
        when(tenantRepository.countByBusinessTypeId(1L)).thenReturn(0L);

        businessTypeService.deleteBusinessType(1L);

        verify(businessTypeRepository).delete(businessType);
    }

    @Test
    void deleteBusinessType_InUse() {
        when(businessTypeRepository.findById(1L)).thenReturn(Optional.of(businessType));
        when(tenantRepository.countByBusinessTypeId(1L)).thenReturn(5L);

        assertThrows(BusinessRuleViolationException.class, () -> businessTypeService.deleteBusinessType(1L));
    }
}
