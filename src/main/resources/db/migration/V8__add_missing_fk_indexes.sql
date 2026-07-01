-- V8: Add missing foreign key index for performance
CREATE INDEX idx_pwd_reset_tokens_user ON password_reset_tokens(user_id);
