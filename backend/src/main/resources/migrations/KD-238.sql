ALTER TABLE answer
    RENAME COLUMN serial_number TO question_serial_number;

ALTER TABLE answer
    ADD COLUMN page_serial_number int;

UPDATE answer a
    SET
        page_serial_number = sp.serial_number
    FROM question q
    JOIN survey_page sp
    ON q.survey_page_id = sp.id
    WHERE a.question_id = q.id;

ALTER TABLE answer
    ALTER COLUMN page_serial_number SET NOT NULL;