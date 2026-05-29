--- Пришлось назвать так, потому что имя 'user' зарезервировано
CREATE TABLE kd_user (
    id uuid PRIMARY KEY,
    login varchar(32) NOT NULL,
    email varchar(254) NOT NULL,
    password_hash text NOT NULL,
    salt text NOT NULL,
    registered_at timestamp NOT NULL
);

CREATE TABLE survey (
    id uuid PRIMARY KEY,
    author_id uuid REFERENCES kd_user (id) NOT NULL,
    title text NOT NULL,
    description text,
    is_authorized_only bool NOT NULL,
    are_responses_limited_to_1 bool NOT NULL,
    is_published bool NOT NULL,
    is_template bool NOT NULL,
    do_notify bool NOT NULL,
    expire_at timestamp,
    created_at timestamp NOT NULL
);

CREATE TABLE permissions (
    user_id uuid REFERENCES kd_user (id),
    survey_id uuid REFERENCES survey (id),
    role text NOT NULL,
    do_notify bool NOT NULL,
    PRIMARY KEY (user_id, survey_id)
);

CREATE TABLE page (
    id uuid PRIMARY KEY,
    survey_id uuid REFERENCES survey (id) NOT NULL,
    serial_number int NOT NULL,
    title text NOT NULL,
    description text
);

CREATE TABLE question (
    id uuid PRIMARY KEY,
    page_id uuid REFERENCES page (id) NOT NULL,
    serial_number int NOT NULL,
    title text NOT NULL,
    description text,
    type text NOT NULL,
    choice_order text NOT NULL,
    is_mandatory bool NOT NULL,
    is_visible bool NOT NULL,
    condition text
);

CREATE TABLE choice (
    id uuid PRIMARY KEY,
    question_id uuid REFERENCES question (id) NOT NULL,
    serial_number int NOT NULL,
    choice text NOT NULL
);

CREATE TABLE completion (
    survey_id uuid PRIMARY KEY REFERENCES survey (id),
    title text NOT NULL,
    description text,
    website_url text
);

CREATE TABLE response (
    id uuid PRIMARY KEY,
    user_id uuid REFERENCES kd_user (id) NOT NULL,
    survey_id uuid REFERENCES survey (id) NOT NULL,
    is_complete bool NOT NULL,
    received_at timestamp NOT NULL
);

CREATE TABLE answer (
    response_id uuid REFERENCES response (id),
    question_id uuid REFERENCES question (id),
    answer text NOT NULL,
    PRIMARY KEY (response_id, question_id)
);