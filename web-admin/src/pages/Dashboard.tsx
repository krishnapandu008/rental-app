import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { getProperties, getMyProperties } from '../api/propertyApi';
import { Property } from '../types';
import PropertyCard from '../components/PropertyCard/PropertyCard';
import styles from './Dashboard.module.scss';

import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

const Dashboard: React.FC = () => {
  const { owner } = useAuth();
  const navigate = useNavigate();

  const [properties, setProperties] = useState<Property[]>([]);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(10);
  const [showMyListings, setShowMyListings] = useState(false);

  const [searchLocation, setSearchLocation] = useState('');
  const [minPrice, setMinPrice] = useState<number | undefined>(undefined);
  const [maxPrice, setMaxPrice] = useState<number | undefined>(undefined);
  const [bedrooms, setBedrooms] = useState<number | undefined>(undefined);
  const [sortBy, setSortBy] = useState('newest');

  const [activeQuickLocation, setActiveQuickLocation] = useState<string>('');

  // ---- Fetch logic ----
  const loadProperties = useCallback(async () => {
    try {
      setLoading(true);
      let res;
      if (showMyListings && owner) {
        res = await getMyProperties(owner.id);
        setProperties(res.data || []);
        setTotalPages(1);
      } else {
        res = await getProperties({
          location: searchLocation || undefined,
          minPrice,
          maxPrice,
          bedrooms,
          sortBy,
          page: currentPage,
          size: pageSize,
        });
        setProperties(res.data?.content || []);
        setTotalPages(res.data?.totalPages || 0);
      }
    } catch (err) {
      console.error(err);
      setProperties([]);
      setTotalPages(0);
    } finally {
      setLoading(false);
    }
  }, [owner, showMyListings, searchLocation, minPrice, maxPrice, bedrooms, sortBy, currentPage, pageSize]);

  // ---- Only update when page, sort, or listing mode changes ----
  useEffect(() => {
    loadProperties();
  }, [currentPage, sortBy, showMyListings]); // deliberately not including filters

  // ---- Trigger a new search with current filters ----
  const performSearch = () => {
    setCurrentPage(0);
    loadProperties(); // will use the current filter state
  };

  // ---- Event handlers ----
  const handleSearchClick = () => {
    setActiveQuickLocation('');
    performSearch();
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();          // prevent any form submission
      setActiveQuickLocation('');
      performSearch();
    }
  };

  const handleQuickLocation = (location: string) => {
    setSearchLocation(location);
    setActiveQuickLocation(location);
    setCurrentPage(0);
    loadProperties();              // immediate fetch
  };

  const handleClearLocation = () => {
    setSearchLocation('');
    setActiveQuickLocation('');
    performSearch();
  };

  const handleReset = () => {
    setSearchLocation('');
    setActiveQuickLocation('');
    setMinPrice(undefined);
    setMaxPrice(undefined);
    setBedrooms(undefined);
    setSortBy('newest');
    setCurrentPage(0);
    if (showMyListings) setShowMyListings(false);
    loadProperties();
  };

  const handlePageChange = (newPage: number) => {
    if (newPage >= 0 && newPage < totalPages) {
      setCurrentPage(newPage);
    }
  };

  const handleOpen = (property: Property) => {
    navigate(`/property/${property.id}`, { state: { property } });
  };

  const toggleMyListings = () => {
    setShowMyListings(!showMyListings);
    setCurrentPage(0);
  };

  if (loading) return <div className={styles.loading}>Loading properties...</div>;

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2 className={styles.title}>Available Rentals</h2>
        {owner && (
          <div className={styles.headerActions}>
            <button className={styles.toggleBtn} onClick={toggleMyListings}>
              {showMyListings ? 'Show All Properties' : 'Show My Listings'}
            </button>
          </div>
        )}
      </div>

      {!showMyListings && (
        <div className={styles.searchForm}>  {/* ✅ No <form> – just a div */}
          {/* Location Group */}
          <div className={styles.locationGroup}>
            <label htmlFor="location">📍 Location</label>
            <div className={styles.locationInputWrapper}>
              <span className={styles.searchIcon}>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <circle cx="11" cy="11" r="8" />
                  <line x1="21" y1="21" x2="16.65" y2="16.65" />
                </svg>
              </span>
              <input
                id="location"
                type="text"
                placeholder="Search by location..."
                value={searchLocation}
                onChange={(e) => {
                  setSearchLocation(e.target.value);
                  setActiveQuickLocation('');
                }}
                onKeyDown={handleKeyDown}   // ✅ Enter triggers search
                className={styles.locationInput}
              />
              {searchLocation && (
                <button
                  type="button"
                  className={styles.clearBtn}
                  onClick={handleClearLocation}
                  aria-label="Clear location"
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <line x1="18" y1="6" x2="6" y2="18" />
                    <line x1="6" y1="6" x2="18" y2="18" />
                  </svg>
                </button>
              )}
            </div>
            <div className={styles.quickChips}>
              <span className={styles.chipLabel}>Quick:</span>
              <button
                type="button"
                className={`${styles.chip} ${activeQuickLocation === '' ? styles.activeChip : ''}`}
                onClick={() => handleQuickLocation('')}
              >
                Anywhere
              </button>
              <button
                type="button"
                className={`${styles.chip} ${activeQuickLocation === 'Kuppam' ? styles.activeChip : ''}`}
                onClick={() => handleQuickLocation('Kuppam')}
              >
                Kuppam
              </button>
              <button
                type="button"
                className={`${styles.chip} ${activeQuickLocation === 'Santhipuram' ? styles.activeChip : ''}`}
                onClick={() => handleQuickLocation('Santhipuram')}
              >
                Santhipuram
              </button>
            </div>
          </div>

          {/* Price and other filters */}
          <div className={styles.filterGroup}>
            <label htmlFor="minPrice">Min Price</label>
            <input
              id="minPrice"
              type="number"
              placeholder="₹ Min"
              value={minPrice ?? ''}
              onChange={(e) => setMinPrice(e.target.value ? Number(e.target.value) : undefined)}
              className={styles.filterInput}
            />
          </div>
          <div className={styles.filterGroup}>
            <label htmlFor="maxPrice">Max Price</label>
            <input
              id="maxPrice"
              type="number"
              placeholder="₹ Max"
              value={maxPrice ?? ''}
              onChange={(e) => setMaxPrice(e.target.value ? Number(e.target.value) : undefined)}
              className={styles.filterInput}
            />
          </div>

          <div className={styles.filterGroup}>
            <label htmlFor="bedrooms">Bedrooms</label>
            <select
              id="bedrooms"
              value={bedrooms ?? ''}
              onChange={(e) => setBedrooms(e.target.value ? Number(e.target.value) : undefined)}
              className={styles.filterSelect}
            >
              <option value="">All</option>
              <option value="1">1 BHK</option>
              <option value="2">2 BHK</option>
              <option value="3">3 BHK</option>
              <option value="4">4+ BHK</option>
            </select>
          </div>

          <div className={styles.filterGroup}>
            <label htmlFor="sortBy">Sort By</label>
            <select
              id="sortBy"
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className={styles.filterSelect}
            >
              <option value="newest">Newest First</option>
              <option value="price_asc">Price: Low to High</option>
              <option value="price_desc">Price: High to Low</option>
            </select>
          </div>

          {/* ✅ Search button – type="button" – no form submission */}
          <button type="button" className={styles.searchBtn} onClick={handleSearchClick}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
            Search
          </button>
          <button type="button" className={styles.resetBtn} onClick={handleReset}>
            Reset
          </button>
        </div>
      )}

      <div className={styles.mapContainer}>
        <MapContainer
          center={[20.5937, 78.9629]}
          zoom={5}
          style={{ height: '400px', width: '100%' }}
        >
          <TileLayer
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          />
          {properties.filter(p => p.latitude && p.longitude).map((p) => (
            <Marker key={p.id} position={[p.latitude!, p.longitude!]}>
              <Popup>
                <strong>{p.title}</strong><br />
                {p.location}<br />
                ₹{p.rent}/month<br />
                <a href={`/property/${p.id}`}>View Details</a>
              </Popup>
            </Marker>
          ))}
        </MapContainer>
      </div>

      {properties.length === 0 ? (
        <div className={styles.emptyState}>
          <p>No properties found matching your criteria.</p>
          {owner && <a href="/add">Add your first property →</a>}
        </div>
      ) : (
        <>
          <div className={styles.propertyGrid}>
            {properties.map((prop) => (
              <PropertyCard
                key={prop.id}
                property={prop}
                onOpen={handleOpen}
              />
            ))}
          </div>

          {!showMyListings && totalPages > 1 && (
            <div className={styles.pagination}>
              <button
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 0}
                className={styles.pageBtn}
              >
                Previous
              </button>
              <span className={styles.pageInfo}>
                Page {currentPage + 1} of {totalPages}
              </span>
              <button
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage === totalPages - 1}
                className={styles.pageBtn}
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default Dashboard;