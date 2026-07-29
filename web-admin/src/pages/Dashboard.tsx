import React, { useEffect, useState, useCallback, lazy, Suspense } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { getProperties, getMyProperties } from '../api/propertyApi';
import { Property } from '../types';
import PropertyCard from '../components/PropertyCard/PropertyCard';
import styles from './Dashboard.module.scss';

// ✅ Lazy load the map component
const PropertyMap = lazy(() => import('../components/PropertyMap'));

const Dashboard: React.FC = () => {
  const { owner } = useAuth();
  const navigate = useNavigate();

  const [properties, setProperties] = useState<Property[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(10);
  const [showMyListings, setShowMyListings] = useState(false);
  const [viewMode, setViewMode] = useState<'list' | 'map'>('list');

  const [searchLocation, setSearchLocation] = useState('');
  const [minPrice, setMinPrice] = useState<number | undefined>(undefined);
  const [maxPrice, setMaxPrice] = useState<number | undefined>(undefined);
  const [bedrooms, setBedrooms] = useState<number | undefined>(undefined);
  const [sortBy, setSortBy] = useState('newest');

  const [activeQuickLocation, setActiveQuickLocation] = useState<string>('');
  
  const [mapCenter, setMapCenter] = useState<[number, number]>([20.5937, 78.9629]);
  const [mapZoom, setMapZoom] = useState(5);

  const loadProperties = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      let res;
      if (showMyListings && owner) {
        res = await getMyProperties(owner.id);
        const data = res.data || [];
        setProperties(data);
        setTotalPages(1);
        
        const propsWithCoords = data.filter((p: Property) => p.latitude && p.longitude);
        if (propsWithCoords.length > 0) {
          const avgLat = propsWithCoords.reduce((sum: number, p: Property) => sum + p.latitude!, 0) / propsWithCoords.length;
          const avgLng = propsWithCoords.reduce((sum: number, p: Property) => sum + p.longitude!, 0) / propsWithCoords.length;
          setMapCenter([avgLat, avgLng]);
          setMapZoom(propsWithCoords.length === 1 ? 14 : 12);
        }
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
        
        const data = res.data?.content || [];
        setProperties(data);
        setTotalPages(res.data?.totalPages || 0);
        
        const propsWithCoords = data.filter((p: Property) => p.latitude && p.longitude);
        if (propsWithCoords.length > 0) {
          const avgLat = propsWithCoords.reduce((sum: number, p: Property) => sum + p.latitude!, 0) / propsWithCoords.length;
          const avgLng = propsWithCoords.reduce((sum: number, p: Property) => sum + p.longitude!, 0) / propsWithCoords.length;
          setMapCenter([avgLat, avgLng]);
          setMapZoom(propsWithCoords.length === 1 ? 14 : 12);
        }
      }
    } catch (err: any) {
      console.error('❌ API Error:', err);
      setError(err.response?.data?.message || err.message || 'Failed to load properties');
      setProperties([]);
      setTotalPages(0);
    } finally {
      setLoading(false);
    }
  }, [owner, showMyListings, searchLocation, minPrice, maxPrice, bedrooms, sortBy, currentPage, pageSize]);

  useEffect(() => {
    loadProperties();
  }, [currentPage, sortBy, showMyListings]);

  const performSearch = () => {
    setCurrentPage(0);
    loadProperties();
  };

  const handleSearchClick = () => {
    setActiveQuickLocation('');
    performSearch();
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      setActiveQuickLocation('');
      performSearch();
    }
  };

  const handleQuickLocation = (location: string) => {
    setSearchLocation(location);
    setActiveQuickLocation(location);
    setCurrentPage(0);
    loadProperties();
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

  const handleMapPropertyClick = (id: number) => {
    const property = properties.find(p => p.id === id);
    if (property) {
      navigate(`/property/${property.id}`, { state: { property } });
    }
  };

  const toggleMyListings = () => {
    setShowMyListings(!showMyListings);
    setCurrentPage(0);
  };

  // ❌ REMOVED: getPropertiesWithCoords - not needed

  if (loading) return <div className={styles.loading}>Loading properties...</div>;

  if (error) {
    return (
      <div className={styles.container}>
        <div className={styles.errorState}>
          <p>❌ {error}</p>
          <button onClick={loadProperties} className={styles.retryBtn}>
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2 className={styles.title}>Available Rentals</h2>
        <div className={styles.headerActions}>
          <div className={styles.viewToggle}>
            <button
              className={`${styles.toggleViewBtn} ${viewMode === 'list' ? styles.activeView : ''}`}
              onClick={() => setViewMode('list')}
            >
              📋 List
            </button>
            <button
              className={`${styles.toggleViewBtn} ${viewMode === 'map' ? styles.activeView : ''}`}
              onClick={() => setViewMode('map')}
            >
              🗺️ Map
            </button>
          </div>
          {owner && (
            <button className={styles.toggleBtn} onClick={toggleMyListings}>
              {showMyListings ? 'Show All Properties' : 'Show My Listings'}
            </button>
          )}
        </div>
      </div>

      {!showMyListings && (
        <div className={styles.searchForm}>
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
                onKeyDown={handleKeyDown}
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

      {/* ===== MAP VIEW ===== */}
      {viewMode === 'map' && (
        <Suspense fallback={
          <div className={styles.loading}>Loading map...</div>
        }>
          <PropertyMap
            properties={properties}
            center={mapCenter}
            zoom={mapZoom}
            onPropertyClick={handleMapPropertyClick}
          />
        </Suspense>
      )}

      {/* ===== LIST VIEW ===== */}
      {viewMode === 'list' && (
        <>
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
        </>
      )}
    </div>
  );
};

export default Dashboard;