CREATE TABLE account (
    id uuid PRIMARY KEY,
    login varchar(32) NOT NULL UNIQUE,
    email varchar(254) NOT NULL UNIQUE,
    password_hash text NOT NULL,
    registered_at timestamptz NOT NULL,
    token_version int NOT NULL,
    is_deleted bool NOT NULL
);

CREATE TABLE survey
(
    id                         uuid PRIMARY KEY,
    author_id                  uuid REFERENCES account (id) ON DELETE CASCADE NOT NULL,
    title                      varchar(200)                                   NOT NULL,
    description                text,
    is_authorized_only         bool                                           NOT NULL,
    is_limited_to_one_response bool                                           NOT NULL,
    is_published               bool                                           NOT NULL,
    is_template                bool                                           NOT NULL,
    do_notify                  bool                                           NOT NULL,
    expire_at                  timestamptz,
    target_timezone            varchar(255),
    created_at                 timestamptz                                    NOT NULL
);

CREATE INDEX idx_survey_author_id
    ON survey (author_id);

CREATE TABLE permission
(
    survey_id  uuid REFERENCES survey (id) ON DELETE CASCADE,
    account_id uuid REFERENCES account (id) ON DELETE CASCADE,
    role       varchar(255)      NOT NULL,
    do_notify  bool DEFAULT TRUE NOT NULL,
    PRIMARY KEY (account_id, survey_id)
);

CREATE TABLE survey_page
(
    id            uuid PRIMARY KEY,
    survey_id     uuid REFERENCES survey (id) ON DELETE CASCADE NOT NULL,
    serial_number int                                           NOT NULL,
    title         varchar(200),
    description   text,
    CONSTRAINT uq_page_survey_serial UNIQUE (survey_id, serial_number) DEFERRABLE INITIALLY IMMEDIATE
);

CREATE INDEX idx_survey_page_survey_id
    ON survey_page (survey_id);

CREATE TABLE question (
    id uuid PRIMARY KEY,
    survey_page_id uuid REFERENCES survey_page (id) ON DELETE CASCADE NOT NULL,
    serial_number int NOT NULL,
    title varchar(200) NOT NULL,
    description varchar(5000),
    attachment_object_key varchar(1024),
    type                  varchar(255)                                       NOT NULL,
    answer_option_order   varchar(255),
    is_mandatory          bool                                               NOT NULL,
    is_visible            bool                                               NOT NULL,
    condition             text,
    CONSTRAINT uq_question_page_serial UNIQUE (survey_page_id, serial_number) DEFERRABLE INITIALLY IMMEDIATE
);

CREATE INDEX idx_question_survey_page_id
    ON question (survey_page_id);

CREATE TABLE answer_option
(
    id                    uuid PRIMARY KEY,
    question_id           uuid REFERENCES question (id) ON DELETE CASCADE NOT NULL,
    serial_number         int                                             NOT NULL,
    answer_option_text    text                                            NOT NULL,
    attachment_object_key varchar(1024),
    CONSTRAINT uq_answer_option_question_serial UNIQUE (question_id, serial_number) DEFERRABLE INITIALLY IMMEDIATE

);

CREATE TABLE closing_page
(
    survey_id             uuid PRIMARY KEY REFERENCES survey (id) ON DELETE CASCADE,
    title                 varchar(200),
    description           text,
    attachment_object_key varchar(1024),
    file_object_key       varchar(1024),
    website_url           varchar(2000)
);

CREATE TABLE response
(
    id           uuid PRIMARY KEY,
    account_id   uuid                                          REFERENCES account (id) ON DELETE SET NULL,
    survey_id    uuid REFERENCES survey (id) ON DELETE CASCADE NOT NULL,
    is_completed bool                                          NOT NULL,
    received_at  timestamptz
);

CREATE INDEX idx_response_survey_id
    ON response (survey_id);

CREATE INDEX idx_response_account_id
    ON response (account_id);

CREATE TABLE answer
(
    response_id uuid REFERENCES response (id) ON DELETE CASCADE,
    question_id uuid REFERENCES question (id) ON DELETE CASCADE,
    answer_text text NOT NULL,
    PRIMARY KEY (response_id, question_id)
);

CREATE INDEX idx_answer_question_id
    ON answer (question_id);

CREATE TABLE survey_notification_subscription
(
    survey_id  uuid REFERENCES survey (id) ON DELETE CASCADE  NOT NULL,
    account_id uuid REFERENCES account (id) ON DELETE CASCADE NOT NULL,
    PRIMARY KEY (survey_id, account_id)
);

CREATE INDEX idx_subscription_survey ON survey_notification_subscription (survey_id);
CREATE INDEX idx_subscription_account ON survey_notification_subscription (account_id);

CREATE TABLE notification_schedule
(
    id              uuid PRIMARY KEY,
    survey_id       uuid REFERENCES survey (id) ON DELETE CASCADE NOT NULL,
    name            varchar(255)                                  NOT NULL,
    schedule_type   varchar(255)                                  NOT NULL,
    -- Для WEEKLY: битовая маска
    days_of_week    int,
    -- Для MONTHLY: день месяца
    day_of_month    int,
    -- Для CUSTOM: cron выражение
    cron_expression varchar(100),
    execution_time  time,
    target_timezone varchar(50) DEFAULT 'Europe/Moscow',
    is_active       boolean     DEFAULT TRUE,
    next_execution  timestamptz,
    last_execution  timestamptz
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

CREATE INDEX idx_refresh_token_account_id ON refresh_token(account_id);
CREATE INDEX idx_refresh_token_account_device ON refresh_token(account_id, device_id);
CREATE INDEX idx_refresh_token_expires_at ON refresh_token(expires_at);
