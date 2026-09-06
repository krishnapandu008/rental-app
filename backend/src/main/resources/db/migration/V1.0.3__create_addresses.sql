-- ================================================================
-- CREATE ADDRESSES TABLE
-- ================================================================

CREATE TABLE IF NOT EXISTS addresses (
    id BIGSERIAL PRIMARY KEY,
    location_id BIGINT REFERENCES locations(id),
    street_name VARCHAR(255),
    street_number VARCHAR(50),
    area_name VARCHAR(100),
    landmark VARCHAR(255),
    formatted_address TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- Create index on location_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_addresses_location_id ON addresses(location_id);