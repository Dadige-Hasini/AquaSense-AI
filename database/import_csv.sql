-- Script to ingest b-a-steinhagen dataset directly in PostgreSQL
-- Usage in psql: \i import_csv.sql
-- Or via Docker: docker exec -i aquasense-postgres psql -U aquasense -d aquasense_db < database/import_csv.sql

-- 1. Truncate staging table
TRUNCATE TABLE reservoir_csv_staging;

-- 2. Copy CSV into staging
-- Adjust path if running inside Docker container or host
\copy reservoir_csv_staging(date, reservoir_storage, conservation_capacity, percent_full) FROM 'data/b-a-steinhagen.csv' WITH (FORMAT csv, HEADER true);

-- 3. Sync staging to main reservoir_records table
CALL sync_staging_to_reservoir_records();

-- 4. Check total imported rows
SELECT COUNT(*) AS total_records_imported FROM reservoir_records WHERE reservoir_name = 'B.A. Steinhagen';
