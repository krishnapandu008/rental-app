-- ================================================================
-- CREATE RENTERS, USERS, AND COLLECTION TABLES
-- ================================================================

-- ---------- renters ----------
CREATE TABLE IF NOT EXISTS renters (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    avatar_url TEXT,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    is_verified BOOLEAN DEFAULT FALSE,
    last_login_at TIMESTAMP,
    preferred_location VARCHAR(255),
    max_rent DOUBLE PRECISION,
    is_active BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- ---------- users ----------
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    avatar_url TEXT,
    role VARCHAR(50) NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    last_login_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- ---------- renter_saved_searches ----------
CREATE TABLE IF NOT EXISTS renter_saved_searches (
    renter_id BIGINT NOT NULL REFERENCES renters(id) ON DELETE CASCADE,
    search_query VARCHAR(255) NOT NULL
);

-- ---------- renter_preferred_amenities ----------
CREATE TABLE IF NOT EXISTS renter_preferred_amenities (
    renter_id BIGINT NOT NULL REFERENCES renters(id) ON DELETE CASCADE,
    preferred_amenity VARCHAR(255) NOT NULL
);

-- ---------- indexes ----------
CREATE INDEX IF NOT EXISTS idx_renters_email ON renters(email);
CREATE INDEX IF NOT EXISTS idx_renters_is_active ON renters(is_active);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);
CREATE INDEX IF NOT EXISTS idx_renter_saved_searches_renter_id ON renter_saved_searches(renter_id);
CREATE INDEX IF NOT EXISTS idx_renter_preferred_amenities_renter_id ON renter_preferred_amenities(renter_id);