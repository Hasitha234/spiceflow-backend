-- V6: Drop the legacy role string column since the new RBAC system is fully integrated.
ALTER TABLE users DROP COLUMN role;
