package com.spiceflow.backend.audit;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for recording and retrieving workflow execution audit projections.
 * Strictly append-only; never updates or deletes historical records.
 */
@Service
public class AuditService {

    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    /**
     * Records an immutable audit projection entry.
     */
    @Transactional
    public AuditEntry record(AuditEntry entry) {
        return auditRepository.save(entry);
    }

    /**
     * Retrieves the chronological audit timeline for a specific workflow correlation ID.
     */
    @Transactional(readOnly = true)
    public List<AuditEntry> getTimeline(Long tenantId, String correlationId) {
        return auditRepository.findByTenantIdAndCorrelationIdOrderByTimestampAsc(tenantId, correlationId);
    }
}
