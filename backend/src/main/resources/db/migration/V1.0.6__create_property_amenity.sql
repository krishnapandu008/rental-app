-- ================================================================
-- CREATE THE CORRECT JOIN TABLE FOR PROPERTY-AMENITY MANY-TO-MANY
-- ================================================================

-- Drop the old table if it exists (we can keep it, but it's not used)
-- DROP TABLE IF EXISTS property_amenities;

-- Create the correct join table
CREATE TABLE IF NOT EXISTS property_amenity (
    property_id BIGINT NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    amenity_id BIGINT NOT NULL REFERENCES amenities(id) ON DELETE CASCADE,
    PRIMARY KEY (property_id, amenity_id)
);

-- Create indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_property_amenity_property_id ON property_amenity(property_id);
CREATE INDEX IF NOT EXISTS idx_property_amenity_amenity_id ON property_amenity(amenity_id);