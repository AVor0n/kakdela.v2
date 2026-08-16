DELETE FROM account WHERE is_hh_sso = true;

ALTER TABLE account
    ADD COLUMN hh_user_id varchar(255);

CREATE UNIQUE INDEX account_hh_user_id_uq
    ON account (hh_user_id)
    WHERE hh_user_id IS NOT NULL;

ALTER TABLE account
DROP COLUMN is_hh_sso;
