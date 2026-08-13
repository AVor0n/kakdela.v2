CREATE TABLE template_bookmark (
    id uuid PRIMARY KEY,
    account_id uuid NOT NULL REFERENCES account (id) ON DELETE CASCADE,
    template_id uuid NOT NULL REFERENCES survey (id) ON DELETE CASCADE,
    created_at timestamptz NOT NULL,
    UNIQUE (account_id, template_id)
);

CREATE INDEX idx_template_bookmark_account_id
ON template_bookmark (account_id);

CREATE INDEX idx_template_bookmark_template_id
ON template_bookmark (template_id);
