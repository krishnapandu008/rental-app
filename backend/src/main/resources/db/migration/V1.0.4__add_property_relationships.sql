-- ================================================================
-- ADD MISSING COLUMNS TO PROPERTIES TABLE
-- ================================================================

-- Foreign keys to new tables
ALTER TABLE properties ADD COLUMN IF NOT EXISTS location_id BIGINT REFERENCES locations(id);
ALTER TABLE properties ADD COLUMN IF NOT EXISTS property_type_id BIGINT REFERENCES property_types(id);
ALTER TABLE properties ADD COLUMN IF NOT EXISTS address_id BIGINT REFERENCES addresses(id);

-- ================================================================
-- BACKFILL DEFAULT VALUES FOR EXISTING ROWS
-- ================================================================

-- Set default location (Kuppam) for existing properties
UPDATE properties
SET location_id = (SELECT id FROM locations WHERE location_id = 'KUPPAM_DEFAULT' LIMIT 1)
WHERE location_id IS NULL
AND EXISTS (SELECT 1 FROM locations WHERE location_id = 'KUPPAM_DEFAULT');

-- Set default property type (Apartment) for existing properties
UPDATE properties
SET property_type_id = (SELECT id FROM property_types WHERE type_name = 'Apartment' LIMIT 1)
WHERE property_type_id IS NULL
AND EXISTS (SELECT 1 FROM property_types WHERE type_name = 'Apartment');

-- Index on location_id (now added)
CREATE INDEX IF NOT EXISTS idx_properties_location_id ON properties(location_id);

-- Spatial index (requires latitude/longitude – they already exist)
CREATE INDEX IF NOT EXISTS idx_properties_geography ON properties 
USING GIST (geography(ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)))
WHERE latitude IS NOT NULL AND longitude IS NOT NULL;