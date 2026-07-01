-- V10: Add Spring Data JPA Auditing columns to all BaseEntity tables

ALTER TABLE platform_admins ADD COLUMN created_by VARCHAR(255);
ALTER TABLE platform_admins ADD COLUMN last_modified_by VARCHAR(255);

ALTER TABLE tenants ADD COLUMN created_by VARCHAR(255);
ALTER TABLE tenants ADD COLUMN last_modified_by VARCHAR(255);

ALTER TABLE users ADD COLUMN created_by VARCHAR(255);
ALTER TABLE users ADD COLUMN last_modified_by VARCHAR(255);

ALTER TABLE roles ADD COLUMN created_by VARCHAR(255);
ALTER TABLE roles ADD COLUMN last_modified_by VARCHAR(255);




