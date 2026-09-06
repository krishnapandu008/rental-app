-- ================================================================
-- CREATE LOCATIONS AND PROPERTY TYPES TABLES
-- ================================================================

-- Locations table
CREATE TABLE IF NOT EXISTS locations (
    id BIGSERIAL PRIMARY KEY,
    location_id VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    district VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    pin_code VARCHAR(10),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    display_order INTEGER DEFAULT 0,
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- Property Types table
CREATE TABLE IF NOT EXISTS property_types (
    id BIGSERIAL PRIMARY KEY,
    type_name VARCHAR(100) UNIQUE NOT NULL,
    icon VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- Insert a default property type if none exists
INSERT INTO property_types (type_name, icon)
SELECT 'Apartment', '🏢'
WHERE NOT EXISTS (SELECT 1 FROM property_types);

-- Insert a default location if none exists (Kuppam)
INSERT INTO locations (location_id, display_name, district, state, country, pin_code, latitude, longitude, is_default)
SELECT 'KUPPAM_DEFAULT', 'Kuppam', 'Kuppam', 'Andhra Pradesh', 'India', '517425', 12.7504, 78.3449, true
WHERE NOT EXISTS (SELECT 1 FROM locations);