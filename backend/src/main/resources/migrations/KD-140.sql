ALTER TABLE survey
    ALTER COLUMN description TYPE text;

ALTER TABLE survey_page
    ALTER COLUMN description TYPE text;

ALTER TABLE question
    ALTER COLUMN description TYPE text;

ALTER TABLE closing_page
    ALTER COLUMN description TYPE text;

ALTER TABLE answer_option
    ALTER COLUMN answer_option_text TYPE text;

ALTER TABLE answer
    ALTER COLUMN answer_text TYPE text;
