CREATE TABLE account (
    id uuid PRIMARY KEY,
    login varchar(32) NOT NULL UNIQUE,
    email varchar(254) NOT NULL UNIQUE,
    password_hash text NOT NULL,
    hh_user_id varchar(255) UNIQUE,
    registered_at timestamptz NOT NULL,
    token_version int NOT NULL,
    is_deleted bool NOT NULL
);

CREATE TABLE survey (
    id uuid PRIMARY KEY,
    author_id uuid REFERENCES account (id) ON DELETE CASCADE NOT NULL,
    title varchar(200) NOT NULL,
    description text,
    attachment_object_key varchar(1024),
    is_authorized_only bool DEFAULT FALSE NOT NULL,
    is_limited_to_one_response bool DEFAULT FALSE NOT NULL,
    is_published bool DEFAULT FALSE NOT NULL,
    is_template bool DEFAULT FALSE NOT NULL,
    do_notify bool DEFAULT TRUE NOT NULL,
    expire_at timestamptz,
    target_timezone varchar(255) DEFAULT 'Europe/Moscow' NOT NULL,
    created_at timestamptz NOT NULL
);

CREATE INDEX idx_survey_author_id
ON survey (author_id);

CREATE TABLE permission (
    survey_id uuid REFERENCES survey (id) ON DELETE CASCADE,
    account_id uuid REFERENCES account (id) ON DELETE CASCADE,
    role varchar(255) NOT NULL,
    do_notify bool DEFAULT TRUE NOT NULL,
    PRIMARY KEY (account_id, survey_id)
);

CREATE TABLE survey_page (
    id uuid PRIMARY KEY,
    survey_id uuid REFERENCES survey (id) ON DELETE CASCADE NOT NULL,
    serial_number int NOT NULL,
    title varchar(200),
    description text,
    CONSTRAINT uk_page_survey_serial UNIQUE (survey_id, serial_number) DEFERRABLE INITIALLY IMMEDIATE
);

CREATE INDEX idx_survey_page_survey_id
ON survey_page (survey_id);

CREATE INDEX idx_survey_page_survey_id_survey_serial_number
ON survey_page(survey_id, serial_number);

CREATE TABLE question (
    id uuid PRIMARY KEY,
    survey_page_id uuid REFERENCES survey_page (id) ON DELETE CASCADE NOT NULL,
    serial_number int NOT NULL,
    text varchar(200) NOT NULL,
    description text,
    attachment_object_key varchar(1024),
    type varchar(255) NOT NULL,
    answer_option_order varchar(255) DEFAULT 'ORIGINAL' NOT NULL,
    has_other_option bool DEFAULT FALSE NOT NULL,
    is_mandatory bool DEFAULT TRUE NOT NULL,
    CONSTRAINT uk_question_page_serial UNIQUE (survey_page_id, serial_number) DEFERRABLE INITIALLY IMMEDIATE
);

CREATE INDEX idx_question_survey_page_id
ON question (survey_page_id);

CREATE TABLE answer_option (
    id uuid PRIMARY KEY,
    question_id uuid REFERENCES question (id) ON DELETE CASCADE NOT NULL,
    serial_number int NOT NULL,
    text text NOT NULL,
    attachment_object_key varchar(1024),
    CONSTRAINT uk_answer_option_question_serial UNIQUE (question_id, serial_number) DEFERRABLE INITIALLY IMMEDIATE

);

CREATE TABLE closing_page (
    survey_id uuid PRIMARY KEY REFERENCES survey (id) ON DELETE CASCADE,
    title varchar(200),
    description text,
    attachment_object_key varchar(1024),
    file_object_key varchar(1024),
    website_url varchar(2000)
);

CREATE TABLE response (
    id uuid PRIMARY KEY,
    account_id uuid REFERENCES account (id) ON DELETE SET NULL,
    survey_id uuid REFERENCES survey (id) ON DELETE CASCADE NOT NULL,
    is_completed bool DEFAULT FALSE NOT NULL,
    received_at timestamptz
);

CREATE INDEX idx_response_survey_id
ON response (survey_id);

CREATE INDEX idx_response_account_id
ON response (account_id);

CREATE TABLE response_page_status (
    id uuid PRIMARY KEY,
    response_id uuid REFERENCES response (id) ON DELETE CASCADE NOT NULL,
    survey_page_id uuid REFERENCES survey_page (id) ON DELETE CASCADE NOT NULL,
    is_included bool DEFAULT FALSE NOT NULL
);

CREATE INDEX idx_response_page_status_response_id
ON response_page_status (response_id);

CREATE INDEX idx_response_page_status_response_id_survey_page_id
ON response_page_status (response_id, survey_page_id);

CREATE TABLE answer (
    id uuid PRIMARY KEY,
    response_id uuid REFERENCES response (id) ON DELETE CASCADE,
    question_id uuid REFERENCES question (id) ON DELETE SET NULL,
    page_serial_number int NOT NULL,
    question_serial_number int NOT NULL,
    question_text_snapshot varchar(200) NOT NULL,
    text_value varchar(5000),
    boolean_value bool,
    date_value date,
    time_value time
);

CREATE INDEX idx_answer_response_id
ON answer (response_id);

CREATE INDEX idx_answer_question_id
ON answer (question_id);

CREATE TABLE selected_answer_option (
    id uuid PRIMARY KEY,
    answer_id uuid REFERENCES answer (id) ON DELETE CASCADE,
    answer_option_id uuid REFERENCES answer_option (id) ON DELETE SET NULL,
    serial_number int NOT NULL,
    answer_option_text_snapshot varchar(1000) NOT NULL
);

CREATE INDEX idx_selected_answer_option_answer_id
ON selected_answer_option (answer_id);

CREATE TABLE condition (
    id uuid PRIMARY KEY,
    survey_page_id uuid REFERENCES survey_page (id) ON DELETE CASCADE NOT NULL,
    next_page_id uuid REFERENCES survey_page (id) ON DELETE CASCADE NOT NULL,
    root_node_id uuid UNIQUE,
    is_active bool DEFAULT FALSE NOT NULL
);

CREATE UNIQUE INDEX idx_condition_page_next_page_active
ON condition (survey_page_id, next_page_id)
WHERE is_active = TRUE;

CREATE INDEX idx_condition_survey_page_id
ON condition (survey_page_id);

CREATE TABLE condition_node (
    id uuid PRIMARY KEY,
    condition_id uuid REFERENCES condition (id) ON DELETE CASCADE NOT NULL,
    parent_node_id uuid REFERENCES condition_node (id) ON DELETE CASCADE,
    operator varchar(255) NOT NULL
);

CREATE INDEX idx_condition_node_parent_node_id
ON condition_node (parent_node_id);

ALTER TABLE condition
ADD CONSTRAINT fk_condition_root_node
FOREIGN KEY (root_node_id)
REFERENCES condition_node (id);

CREATE TABLE condition_atom (
    condition_node_id uuid PRIMARY KEY REFERENCES condition_node (id) ON DELETE CASCADE,
    question_id uuid REFERENCES question (id) ON DELETE CASCADE NOT NULL,
    operator varchar(255) NOT NULL,
    required_boolean_value bool,
    required_answer_option_id uuid REFERENCES answer_option (id) ON DELETE CASCADE
);

CREATE TABLE survey_notification_subscription (
    survey_id uuid REFERENCES survey (id) ON DELETE CASCADE NOT NULL,
    account_id uuid REFERENCES account (id) ON DELETE CASCADE NOT NULL,
    PRIMARY KEY (survey_id, account_id)
);

CREATE INDEX idx_subscription_survey
ON survey_notification_subscription (survey_id);

CREATE INDEX idx_subscription_account
ON survey_notification_subscription (account_id);

CREATE TABLE notification_schedule (
    id uuid PRIMARY KEY,
    survey_id uuid REFERENCES survey(id) ON DELETE CASCADE NOT NULL,
    name varchar(255) NOT NULL,
    schedule_type varchar(255) NOT NULL,
    -- Для WEEKLY: битовая маска
    days_of_week int,
    -- Для MONTHLY: день месяца
    day_of_month int,
    -- Для CUSTOM: cron выражение
    cron_expression varchar(100),
    execution_time time,
    target_timezone varchar(50) DEFAULT 'Europe/Moscow' NOT NULL,
    is_active bool DEFAULT TRUE NOT NULL,
    next_execution timestamptz,
    last_execution timestamptz
);

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
