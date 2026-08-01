ALTER TABLE account DROP COLUMN IF EXISTS hh_user_id;
ALTER TABLE account ADD COLUMN is_hh_sso boolean NOT NULL DEFAULT false;