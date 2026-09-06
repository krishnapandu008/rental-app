-- ================================================================
-- SAFE INDEXES FOR EXISTING COLUMNS
-- ================================================================

CREATE INDEX IF NOT EXISTS idx_properties_rent ON properties(rent);
CREATE INDEX IF NOT EXISTS idx_properties_bedrooms ON properties(bedrooms);
CREATE INDEX IF NOT EXISTS idx_properties_visibility ON properties(visibility);
CREATE INDEX IF NOT EXISTS idx_properties_is_active ON properties(is_active);
CREATE INDEX IF NOT EXISTS idx_properties_owner_id ON properties(owner_id);
CREATE INDEX IF NOT EXISTS idx_properties_visibility_active ON properties(visibility, is_active);
CREATE INDEX IF NOT EXISTS idx_properties_owner_active ON properties(owner_id, is_active);