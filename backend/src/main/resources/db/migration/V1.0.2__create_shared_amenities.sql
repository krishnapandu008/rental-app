-- ================================================================
-- CREATE AMENITIES TABLE
-- ================================================================

CREATE TABLE IF NOT EXISTS amenities (
    id BIGSERIAL PRIMARY KEY,
    amenity_name VARCHAR(100) UNIQUE NOT NULL,
    icon VARCHAR(50),
    category VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- Insert default amenities (safe to run repeatedly)
INSERT INTO amenities (amenity_name, icon, category)
SELECT * FROM (VALUES
    ('parking', '🅿️', 'facility'),
    ('furnished', '🛋️', 'interior'),
    ('ac', '❄️', 'facility'),
    ('security', '🔒', 'safety'),
    ('gym', '💪', 'facility'),
    ('swimming_pool', '🏊', 'facility'),
    ('garden', '🌿', 'outdoor'),
    ('wifi', '📶', 'utility'),
    ('pet_friendly', '🐾', 'policy'),
    ('water_supply', '💧', 'utility'),
    ('power_backup', '⚡', 'utility'),
    ('lift', '🛗', 'facility')
) AS amenities_data (amenity_name, icon, category)
WHERE NOT EXISTS (SELECT 1 FROM amenities LIMIT 1);