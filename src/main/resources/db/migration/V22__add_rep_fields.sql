-- V22: Add employee ID, email, and dates to Sales Representatives table

ALTER TABLE reps ADD COLUMN employee_id VARCHAR(50);
ALTER TABLE reps ADD COLUMN email VARCHAR(100);
ALTER TABLE reps ADD COLUMN employment_date DATE;
ALTER TABLE reps ADD COLUMN termination_date DATE;

CREATE INDEX idx_reps_employee_id ON reps(tenant_id, employee_id);
