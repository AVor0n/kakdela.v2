CREATE INDEX idx_survey_page_survey_id_survey_serial_number
ON survey_page(survey_id, serial_number);

CREATE TABLE condition (
    id uuid PRIMARY KEY,
    survey_page_id uuid REFERENCES survey_page (id) ON DELETE CASCADE NOT NULL,
    next_page_id uuid REFERENCES survey_page (id) ON DELETE CASCADE NOT NULL,
    root_node_id uuid UNIQUE,
    CONSTRAINT uk_condition_page_next_page UNIQUE (survey_page_id, next_page_id)
);

CREATE INDEX idx_condition_survey_page_id
ON condition (survey_page_id);

CREATE TABLE condition_else_page (
    survey_page_id uuid PRIMARY KEY REFERENCES survey_page (id) ON DELETE CASCADE,
    else_page_id uuid REFERENCES survey_page (id) ON DELETE CASCADE
);

CREATE TABLE condition_node (
    id uuid PRIMARY KEY,
    condition_id uuid REFERENCES condition (id) ON DELETE CASCADE NOT NULL,
    parent_node_id uuid REFERENCES condition_node (id) ON DELETE CASCADE,
    operator varchar(255) NOT NULL
);

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