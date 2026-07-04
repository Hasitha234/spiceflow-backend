-- V32__create_cash_collections_table.sql
-- Creates cash_collections table for ADR-013 workflow FSM governance and receivables accounting

CREATE TABLE cash_collections (
    id BIGSERIAL PRIMARY KEY,
    collection_number VARCHAR(50) NOT NULL UNIQUE,
    correlation_id VARCHAR(50) NOT NULL,
    tenant_id BIGINT NOT NULL,
    shop_id BIGINT NOT NULL,
    rep_id BIGINT,
    collection_date DATE NOT NULL,
    amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    payment_method VARCHAR(30) NOT NULL DEFAULT 'CASH',
    cheque_no VARCHAR(50),
    cheque_bank_name VARCHAR(100),
    cheque_date DATE,
    notes VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    confirmed_by VARCHAR(100),
    cancelled_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_cc_tenant_status ON cash_collections(tenant_id, status);
CREATE INDEX idx_cc_tenant_shop ON cash_collections(tenant_id, shop_id);
CREATE INDEX idx_cc_number_tenant ON cash_collections(collection_number, tenant_id);
