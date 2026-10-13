-- ==============================================================================
-- AquaSense AI Database Initialization Script
-- Target: PostgreSQL 15+
-- Schema: Hydro-Informatics & Reservoir Monitoring for B.A. Steinhagen Lake
-- ==============================================================================

-- Enable UUID extension if needed for distributed identifiers
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ------------------------------------------------------------------------------
-- 1. Water Bodies / Reservoirs Catalog
-- Stores metadata regarding monitored lakes, dams, and river basins
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS water_bodies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    state VARCHAR(50) DEFAULT 'Texas',
    county VARCHAR(100) DEFAULT 'Tyler / Jasper',
    river_basin VARCHAR(100) DEFAULT 'Neches River Basin',
    conservation_capacity DOUBLE PRECISION DEFAULT 69186.0,
    conservation_pool_elevation DOUBLE PRECISION DEFAULT 82.5,
    surface_area DOUBLE PRECISION DEFAULT 13700.0,
    dam_name VARCHAR(100) DEFAULT 'Town Bluff Dam / Dam B',
    managing_agency VARCHAR(100) DEFAULT 'USACE / TWDB',
    latitude DOUBLE PRECISION DEFAULT 30.7989,
    longitude DOUBLE PRECISION DEFAULT -94.1706,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Seed B.A. Steinhagen into catalog
INSERT INTO water_bodies (
    name, state, county, river_basin, conservation_capacity, 
    conservation_pool_elevation, surface_area, dam_name, managing_agency, 
    latitude, longitude, status
) VALUES (
    'B.A. Steinhagen', 'Texas', 'Tyler / Jasper', 'Neches River Basin', 
    69186.0, 82.5, 13700.0, 'Town Bluff Dam / Dam B', 'US Army Corps of Engineers / TWDB',
    30.7989, -94.1706, 'ACTIVE'
) ON CONFLICT (name) DO NOTHING;

-- ------------------------------------------------------------------------------
-- 2. Main Reservoir Observations Table
-- Stores daily historical and real-time hydrological records.
-- Supports columns from b-a-steinhagen-under-1mb (1).csv and extended sensor metrics.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reservoir_records (
    id BIGSERIAL PRIMARY KEY,
    reservoir_name VARCHAR(100) NOT NULL DEFAULT 'B.A. Steinhagen',
    record_date DATE NOT NULL,
    water_level DOUBLE PRECISION,                     -- Elevation in feet above mean sea level
    surface_area DOUBLE PRECISION,                    -- Lake surface area in acres
    reservoir_storage DOUBLE PRECISION,               -- Total reservoir storage in acre-feet
    conservation_storage DOUBLE PRECISION,            -- Conservation storage in acre-feet
    percent_full NUMERIC(5, 2),                       -- Percent full relative to conservation capacity (e.g. 96.60)
    conservation_capacity DOUBLE PRECISION,           -- Normal conservation pool capacity in acre-feet
    source VARCHAR(50) DEFAULT 'TWDB_HISTORICAL',     -- Source agency / sensor provider (TWDB, USACE, USGS)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_reservoir_record UNIQUE (reservoir_name, record_date)
);

-- Indices for rapid querying and time-series aggregation
CREATE INDEX IF NOT EXISTS idx_reservoir_records_date 
    ON reservoir_records (record_date DESC);

CREATE INDEX IF NOT EXISTS idx_reservoir_records_name_date 
    ON reservoir_records (reservoir_name, record_date DESC);

CREATE INDEX IF NOT EXISTS idx_reservoir_records_pct 
    ON reservoir_records (percent_full);

-- ------------------------------------------------------------------------------
-- 2. CSV Staging Table
-- Matches exact column headers of b-a-steinhagen-under-1mb (1).csv for zero-friction ingestion:
-- date,reservoir_storage,conservation_capacity,percent_full
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reservoir_csv_staging (
    date DATE NOT NULL,
    reservoir_storage DOUBLE PRECISION,
    conservation_capacity DOUBLE PRECISION,
    percent_full NUMERIC(5, 2)
);

-- Staging ingestion trigger / sync procedure to populate reservoir_records
CREATE OR REPLACE PROCEDURE sync_staging_to_reservoir_records()
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO reservoir_records (
        reservoir_name,
        record_date,
        reservoir_storage,
        conservation_capacity,
        percent_full,
        source
    )
    SELECT 
        'B.A. Steinhagen' AS reservoir_name,
        date,
        reservoir_storage,
        conservation_capacity,
        percent_full,
        'CSV_IMPORT'
    FROM reservoir_csv_staging
    ON CONFLICT (reservoir_name, record_date) DO UPDATE SET
        reservoir_storage = EXCLUDED.reservoir_storage,
        conservation_capacity = EXCLUDED.conservation_capacity,
        percent_full = EXCLUDED.percent_full,
        updated_at = CURRENT_TIMESTAMP;
END;
$$;

-- ------------------------------------------------------------------------------
-- 3. AI / Machine Learning Predictions Table
-- Stores water level & storage forecasts generated by the FastAPI ML service.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reservoir_predictions (
    id BIGSERIAL PRIMARY KEY,
    reservoir_name VARCHAR(100) NOT NULL DEFAULT 'B.A. Steinhagen',
    forecast_date DATE NOT NULL,                      -- Target forecast date
    predicted_storage DOUBLE PRECISION NOT NULL,      -- Predicted volume in acre-feet
    predicted_percent_full NUMERIC(5, 2),             -- Predicted percentage full
    predicted_water_level DOUBLE PRECISION,           -- Predicted water level elevation
    confidence_interval_lower DOUBLE PRECISION,       -- 95% CI lower bound
    confidence_interval_upper DOUBLE PRECISION,       -- 95% CI upper bound
    model_name VARCHAR(100) NOT NULL,                 -- e.g. 'LSTM_v1', 'XGBoost_WaterLevel_v2', 'Prophet_Daily'
    model_version VARCHAR(50) NOT NULL,
    generated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_prediction_target UNIQUE (reservoir_name, forecast_date, model_name, model_version)
);

CREATE INDEX IF NOT EXISTS idx_predictions_forecast_date 
    ON reservoir_predictions (forecast_date ASC);

CREATE INDEX IF NOT EXISTS idx_predictions_model 
    ON reservoir_predictions (model_name, model_version);

-- ------------------------------------------------------------------------------
-- 4. Analytical Views for Dashboard & Backend Services
-- ------------------------------------------------------------------------------

-- Latest snapshot view
CREATE OR REPLACE VIEW v_latest_reservoir_status AS
SELECT 
    id,
    reservoir_name,
    record_date,
    water_level,
    surface_area,
    reservoir_storage,
    conservation_storage,
    percent_full,
    conservation_capacity,
    updated_at
FROM reservoir_records
WHERE (reservoir_name, record_date) IN (
    SELECT reservoir_name, MAX(record_date)
    FROM reservoir_records
    GROUP BY reservoir_name
);

-- Monthly trend aggregation
CREATE OR REPLACE VIEW v_monthly_reservoir_summary AS
SELECT 
    reservoir_name,
    DATE_TRUNC('month', record_date)::DATE AS summary_month,
    ROUND(AVG(reservoir_storage)::numeric, 2) AS avg_storage,
    ROUND(MIN(reservoir_storage)::numeric, 2) AS min_storage,
    ROUND(MAX(reservoir_storage)::numeric, 2) AS max_storage,
    ROUND(AVG(percent_full)::numeric, 2) AS avg_percent_full,
    COUNT(*) AS record_count
FROM reservoir_records
GROUP BY reservoir_name, DATE_TRUNC('month', record_date)
ORDER BY summary_month DESC;

-- ------------------------------------------------------------------------------
-- 5. Seed Data / Verification Sample
-- Inserts a baseline reference record if table is empty
-- ------------------------------------------------------------------------------
INSERT INTO reservoir_records (
    reservoir_name, 
    record_date, 
    water_level, 
    surface_area, 
    reservoir_storage, 
    conservation_storage, 
    percent_full, 
    conservation_capacity,
    source
) VALUES 
    ('B.A. Steinhagen', '2026-10-13', 82.40, 13700.0, 66931.0, 66931.0, 96.60, 69186.0, 'INITIAL_SEED')
ON CONFLICT (reservoir_name, record_date) DO NOTHING;
