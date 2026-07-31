ALTER TABLE permissions
    RENAME TO permission;

ALTER TABLE permission
    ALTER COLUMN do_notify SET DEFAULT TRUE;