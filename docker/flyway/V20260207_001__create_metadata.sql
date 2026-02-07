-- create table
CREATE TABLE IF NOT EXISTS metadata
(
    key text PRIMARY KEY,
    value text NOT NULL
);

-- insert data
INSERT INTO metadata (key, value) VALUES
    ('db_version', '1')
    ON CONFLICT (key)
    DO UPDATE SET value = EXCLUDED.value
       WHERE metadata.value <> EXCLUDED.value;
