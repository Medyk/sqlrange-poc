-- create table
CREATE TABLE IF NOT EXISTS books
(
    uuid uuid NOT NULL DEFAULT gen_random_uuid(), -- unique "family" identifier
    revision integer NOT NULL DEFAULT 1,        -- revision number for optimistic locking
    revision_from timestamp with time zone NOT NULL DEFAULT '2000-01-01 00:00:00+00:00'::timestamp with time zone,  -- timestamp of creation or last revision
    revision_to timestamp with time zone NOT NULL DEFAULT 'infinity'::timestamp with time zone, -- timestamp of next revision or infinity
    -- https://www.postgresql.org/docs/current/range-types.html
    revision_range tstzrange GENERATED ALWAYS AS (tstzrange(revision_from, revision_to, '[)')) STORED,
    revision_range_cc tstzrange GENERATED ALWAYS AS (tstzrange(revision_from, revision_to, '[]')) STORED,
    -- entity fields
    title text NOT NULL,                        -- title of the book
    author text NOT NULL,                       -- author of the book
    pub_year integer NOT NULL,                  -- publication year of the book
    genre text NOT NULL,                        -- genre of the book
    -- constraints
    CONSTRAINT book_c_uuid PRIMARY KEY (uuid, revision_from),
    CONSTRAINT book_c_unique_revisions_per_uuid UNIQUE (uuid, revision),
    CONSTRAINT book_c_check_revision_positive CHECK (revision >= 1),
    CONSTRAINT book_c_no_empty_revisions CHECK (revision_to > revision_from),
    CONSTRAINT book_c_no_overlapping_revisions EXCLUDE USING gist (
        uuid WITH =,
        revision_range WITH &&
    )
);

-- indexes
CREATE INDEX IF NOT EXISTS book_idx_uuid
    ON books
    USING btree (uuid);
CREATE INDEX IF NOT EXISTS book_idx_revision_from
    ON books
    USING btree (revision_from);
CREATE INDEX IF NOT EXISTS book_idx_revision_to
    ON books
    USING btree (revision_to);
CREATE INDEX IF NOT EXISTS book_idx_revision_range_gist
    ON books
    USING gist (revision_range);
CREATE INDEX IF NOT EXISTS book_idx_revision_range_cc_gist
    ON books
    USING gist (revision_range_cc);
