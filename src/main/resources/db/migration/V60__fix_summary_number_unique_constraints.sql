-- Drop the global unique constraints
ALTER TABLE morning_summaries DROP CONSTRAINT IF EXISTS morning_summaries_summary_number_key;
ALTER TABLE cancel_summaries DROP CONSTRAINT IF EXISTS cancel_summaries_summary_number_key;

-- Add the multi-tenant composite unique constraints
ALTER TABLE morning_summaries ADD CONSTRAINT morning_summaries_tenant_summary_number_key UNIQUE (tenant_id, summary_number);
ALTER TABLE cancel_summaries ADD CONSTRAINT cancel_summaries_tenant_summary_number_key UNIQUE (tenant_id, summary_number);
