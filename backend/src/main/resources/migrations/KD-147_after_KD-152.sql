UPDATE survey
    SET target_timezone = 'Europe/Moscow'
    WHERE target_timezone IS NULL;

ALTER TABLE survey
    ALTER COLUMN is_authorized_only SET DEFAULT FALSE,
    ALTER COLUMN is_limited_to_one_response SET DEFAULT FALSE,
    ALTER COLUMN is_published SET DEFAULT FALSE,
    ALTER COLUMN is_template SET DEFAULT FALSE,
    ALTER COLUMN do_notify SET DEFAULT TRUE,
    ALTER COLUMN target_timezone SET DEFAULT 'Europe/Moscow',
    ALTER COLUMN target_timezone SET NOT NULL;

ALTER TABLE survey_page
    RENAME CONSTRAINT uq_page_survey_serial TO uk_page_survey_serial;

ALTER TABLE question
    RENAME COLUMN title TO text;

ALTER TABLE question
    ALTER COLUMN answer_option_order SET DEFAULT 'ORIGINAL',
    ALTER COLUMN answer_option_order SET NOT NULL,
    ADD COLUMN has_other_option bool DEFAULT FALSE NOT NULL,
    ALTER COLUMN is_mandatory SET DEFAULT TRUE,
    ALTER COLUMN is_visible SET DEFAULT TRUE;

ALTER TABLE question
    RENAME CONSTRAINT uq_question_page_serial TO uk_question_page_serial;

ALTER TABLE answer_option
    RENAME COLUMN answer_option_text TO text;

ALTER TABLE answer_option
    RENAME CONSTRAINT uq_answer_option_question_serial TO uk_answer_option_question_serial;

ALTER TABLE response
    ALTER COLUMN is_completed SET DEFAULT FALSE;

ALTER TABLE answer
    RENAME COLUMN answer_text TO text_value;

ALTER TABLE answer
    ADD COLUMN question_text_snapshot varchar(200) NOT NULL,
    ADD COLUMN id uuid,
    ADD COLUMN boolean_value bool,
    ADD COLUMN date_value date,
    ADD COLUMN time_value time;

UPDATE answer
    SET id = gen_random_uuid()
    WHERE id IS NULL;

ALTER TABLE answer
    ALTER COLUMN id SET NOT NULL;

ALTER TABLE answer
    DROP CONSTRAINT answer_pkey;

ALTER TABLE answer
    ADD CONSTRAINT answer_pkey PRIMARY KEY (id);

CREATE INDEX idx_answer_response_id
ON answer (response_id);

CREATE TABLE selected_answer_option (
    id uuid PRIMARY KEY,
    answer_id uuid REFERENCES answer (id) ON DELETE CASCADE,
    answer_option_id uuid REFERENCES answer_option (id) ON DELETE SET NULL,
    answer_option_text_snapshot varchar(1000) NOT NULL
);

CREATE INDEX idx_selected_answer_option_answer_id
ON selected_answer_option (answer_id);

ALTER TABLE notification_schedule
    ALTER COLUMN target_timezone SET NOT NULL,
    ALTER COLUMN is_active SET NOT NULL;