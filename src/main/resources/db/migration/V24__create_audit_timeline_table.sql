-- V24__create_audit_timeline_table.sql
-- Creates the immutable append-only audit timeline projection table for operational workflows.

CREATE TABLE audit_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    correlation_id VARCHAR(100) NOT NULL,
    command_name VARCHAR(100) NOT NULL,
    from_state VARCHAR(50) NOT NULL,
    to_state VARCHAR(50) NOT NULL,
    comment VARCHAR(1000),
    timestamp TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_entries_tenant_correlation ON audit_entries(tenant_id, correlation_id);
CREATE INDEX idx_audit_entries_tenant_timestamp ON audit_entries(tenant_id, timestamp);
