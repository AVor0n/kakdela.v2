CREATE INDEX idx_survey_page_survey_id_survey_serial_number
ON survey_page(survey_id, serial_number);

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

ALTER TABLE question
    DROP COLUMN is_visible,
    DROP COLUMN condition;

ALTER TABLE survey
    ADD COLUMN attachment_object_key varchar(1024);

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