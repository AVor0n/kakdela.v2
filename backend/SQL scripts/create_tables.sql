--- Пришлось назвать так, потому что имя 'user' зарезервировано
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
    title text NOT NULL,
    description text,
    is_authorized_only bool NOT NULL,
    is_limited_to_one_response bool NOT NULL,
    is_published bool NOT NULL,
    is_template bool NOT NULL,
    do_notify bool NOT NULL,
    expire_at timestamptz,
    created_at timestamptz NOT NULL
);

CREATE TABLE permissions (
    account_id uuid REFERENCES account (id) ON DELETE CASCADE,
    survey_id uuid REFERENCES survey (id) ON DELETE CASCADE,
    role text NOT NULL,
    do_notify bool NOT NULL,
    PRIMARY KEY (account_id, survey_id)
);

CREATE TABLE survey_page (
    id uuid PRIMARY KEY,
    survey_id uuid REFERENCES survey (id) ON DELETE CASCADE NOT NULL,
    serial_number int NOT NULL,
    title text NOT NULL,
    description text
);

CREATE TABLE question (
    id uuid PRIMARY KEY,
    survey_page_id uuid REFERENCES survey_page (id) ON DELETE CASCADE NOT NULL,
    serial_number int NOT NULL,
    title text NOT NULL,
    description text,
    type text NOT NULL,
    answer_option_order text NOT NULL,
    is_mandatory bool NOT NULL,
    is_visible bool NOT NULL,
    condition text
);

CREATE TABLE answer_option (
    id uuid PRIMARY KEY,
    question_id uuid REFERENCES question (id) ON DELETE CASCADE NOT NULL,
    serial_number int NOT NULL,
    answer_option_text text NOT NULL
);

CREATE TABLE closing_page (
    survey_id uuid PRIMARY KEY REFERENCES survey (id) ON DELETE CASCADE,
    title text NOT NULL,
    description text,
    website_url text
);

CREATE TABLE response (
    id uuid PRIMARY KEY,
    account_id uuid REFERENCES account (id) ON DELETE SET NULL,
    survey_id uuid REFERENCES survey (id) ON DELETE CASCADE NOT NULL,
    is_complete bool NOT NULL,
    received_at timestamptz NOT NULL
);

CREATE TABLE answer (
    response_id uuid REFERENCES response (id) ON DELETE CASCADE,
    question_id uuid REFERENCES question (id) ON DELETE CASCADE,
    answer_text text NOT NULL,
    PRIMARY KEY (response_id, question_id)
);