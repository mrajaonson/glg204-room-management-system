ALTER TABLE account_creation_request
    ADD COLUMN updated_at TIMESTAMP;

UPDATE account_creation_request
SET updated_at = created_at
WHERE updated_at IS NULL;

ALTER TABLE account_creation_request
    ALTER COLUMN updated_at SET NOT NULL;
