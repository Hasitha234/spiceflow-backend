-- V60 attempted to drop the global unique constraint on cancel_summaries.summary_number,
-- but used the wrong constraint name (cancel_summaries_summary_number_key instead of
-- uq_cancel_summary_number). This migration drops the correct constraint.
--
-- After this migration, the only unique constraint is the tenant-scoped one:
-- cancel_summaries_tenant_summary_number_key UNIQUE(tenant_id, summary_number)

ALTER TABLE cancel_summaries DROP CONSTRAINT IF EXISTS uq_cancel_summary_number;
