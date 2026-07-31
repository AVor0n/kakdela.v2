CREATE TABLE account (
    id uuid PRIMARY KEY,
    login varchar(32) NOT NULL UNIQUE,
    email varchar(254) NOT NULL UNIQUE,
    password_hash text NOT NULL,
    registered_at timestamptz NOT NULL
);

CREATE TABLE survey (
    id uuid PRIMARY KEY,
    author_id uuid REFERENCES account (id) ON DELETE CASCADE NOT NULL,
    title varchar(200) NOT NULL,
    description text,
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

CREATE TABLE permissions (
    account_id uuid REFERENCES account (id) ON DELETE CASCADE,
    survey_id uuid REFERENCES survey (id) ON DELETE CASCADE,
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
    is_visible bool DEFAULT TRUE NOT NULL,
    condition text,
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

CREATE TABLE answer (
    id uuid PRIMARY KEY,
    response_id uuid REFERENCES response (id) ON DELETE CASCADE,
    question_id uuid REFERENCES question (id) ON DELETE SET NULL,
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
    answer_option_text_snapshot varchar(1000) NOT NULL
);

CREATE INDEX idx_selected_answer_option_answer_id
ON selected_answer_option (answer_id);

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
