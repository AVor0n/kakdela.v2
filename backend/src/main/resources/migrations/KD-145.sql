ALTER TABLE account
    ADD COLUMN token_version int NOT NULL DEFAULT 1,
    ADD COLUMN is_deleted bool NOT NULL DEFAULT FALSE;

CREATE TABLE refresh_token (
    id uuid PRIMARY KEY,
    token_hash varchar(64) UNIQUE NOT NULL,
    account_id uuid REFERENCES account(id) ON DELETE CASCADE NOT NULL,
    device_id varchar(255) NOT NULL,
    user_agent text,
    ip_address varchar(45),
    created_at timestamptz NOT NULL,
    expires_at timestamptz NOT NULL,
    last_used_at timestamptz
);

CREATE INDEX idx_refresh_token_account_id
ON refresh_token(account_id);

CREATE INDEX idx_refresh_token_account_device
ON refresh_token(account_id, device_id);

CREATE INDEX idx_refresh_token_expires_at
ON refresh_token(expires_at);