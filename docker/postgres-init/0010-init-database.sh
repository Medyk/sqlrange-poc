#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
  -- Create docker user
	CREATE ROLE docker LOGIN PASSWORD 'docker';

	-- Create docker database
	CREATE DATABASE docker;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "docker" <<-EOSQL
    -- Create required extensions
    CREATE EXTENSION IF NOT EXISTS btree_gist;
    CREATE EXTENSION IF NOT EXISTS plpgsql;

    -- Create schema
    CREATE SCHEMA IF NOT EXISTS docker;

    -- Add owner role and grant connection
    ALTER DATABASE docker OWNER TO docker;
    ALTER SCHEMA docker OWNER TO docker;

    -- Set ownership and permissions
    GRANT CONNECT ON DATABASE docker TO docker;
    GRANT USAGE ON SCHEMA docker TO docker;
    ALTER ROLE docker SET search_path TO docker, public;
    GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA docker TO docker;
    GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA docker TO docker;
    ALTER DEFAULT PRIVILEGES FOR ROLE docker IN SCHEMA docker GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO docker;
    ALTER DEFAULT PRIVILEGES FOR ROLE docker IN SCHEMA docker GRANT USAGE, SELECT ON SEQUENCES TO docker;
EOSQL
