import React, { useState, useEffect, useCallback, lazy, Suspense, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { getProperties, getMyProperties, voiceSearch } from '../../api/propertyApi';
import { Property } from '../../types';
import { useVoiceSearch } from '../../hooks/useVoiceSearch';
import HeroSection from './components/HeroSection/HeroSection';
import SearchBar from './components/SearchBar/SearchBar';
import QuickChips, { Chip } from './components/QuickChips/QuickChips';
import FiltersPanel from './components/FiltersPanel/FiltersPanel';
import ResultsHeader from './components/ResultsHeader/ResultsHeader';
import ResultsList from './components/ResultsList/ResultsList';
import Pagination from './components/Pagination/Pagination';
import styles from './Dashboard.module.scss';

// Lazy load map
const PropertyMap = lazy(() => import('../../components/PropertyMap.tsx'));

// Types
interface Filters {
  location: string;
  displayLocation: string;
  minPrice: number | undefined;
  maxPrice: number | undefined;
  bedrooms: number | undefined;
  propertyType: string;
  amenities: string[];
  sortBy: string;
}

// Quick chips data
const quickChips: Chip[] = [
  { label: 'Under ₹10K', filter: { maxPrice: 10000 }, icon: '💰' },
  { label: 'Under ₹15K', filter: { maxPrice: 15000 }, icon: '💰' },
  { label: '1 BHK', filter: { bedrooms: 1 }, icon: '🛏️' },
  { label: '2 BHK', filter: { bedrooms: 2 }, icon: '🛏️' },
  { label: '3 BHK', filter: { bedrooms: 3 }, icon: '🛏️' },
  { label: 'Parking', filter: { amenities: ['parking'] }, icon: '🅿️' },
  { label: 'Furnished', filter: { amenities: ['furnished'] }, icon: '🛋️' },
];

const Dashboard: React.FC = () => {
  const { owner } = useAuth();
  const navigate = useNavigate();

  // State
  const [properties, setProperties] = useState<Property[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(10);
  const [showMyListings, setShowMyListings] = useState(false);
  const [viewMode, setViewMode] = useState<'list' | 'map'>('list');
  const [searching, setSearching] = useState(false);
  const [searchProgress, setSearchProgress] = useState('');
  const [aiExplanation, setAiExplanation] = useState<string | null>(null);
  const [showFilters, setShowFilters] = useState(false);
  const [locationInfo, setLocationInfo] = useState<{location: string, district: string, state: string, pinCode: string} | null>(null);

  // Filters state
  const [filters, setFilters] = useState<Filters>({
    location: '',
    displayLocation: '',
    minPrice: undefined,
    maxPrice: undefined,
    bedrooms: undefined,
    propertyType: '',
    amenities: [],
    sortBy: 'newest',
  });

  const [activeQuickChips, setActiveQuickChips] = useState<string[]>([]);
  const [mapCenter] = useState<[number, number]>([20.5937, 78.9629]);
  const mapZoom = 10;

  // ---- Load properties ----
  const loadProperties = useCallback(async (filtersToUse: Filters = filters, page = currentPage) => {
    try {
      setLoading(true);
      setError(null);

      let res;
      if (showMyListings && owner) {
        res = await getMyProperties(owner.id);
        const data = res.data || [];
        setProperties(data);
        setTotalPages(1);
      } else {
        res = await getProperties({
          location: filtersToUse.location || undefined,
          minPrice: filtersToUse.minPrice,
          maxPrice: filtersToUse.maxPrice,
          bedrooms: filtersToUse.bedrooms,
          propertyType: filtersToUse.propertyType || undefined,
          amenities: filtersToUse.amenities?.length ? filtersToUse.amenities : undefined,
          sortBy: filtersToUse.sortBy,
          page,
          size: pageSize,
        });
        const data = res.data?.content || [];
        setProperties(data);
        setTotalPages(res.data?.totalPages || 0);
      }
    } catch (err: any) {
      console.error('API Error:', err);
      setError(err.response?.data?.message || 'Failed to load properties');
      setProperties([]);
      setTotalPages(0);
    } finally {
      setLoading(false);
    }
  }, [owner, showMyListings, filters, currentPage, pageSize]);

  // ---- Perform search ----
  const performSearch = useCallback(() => {
    if (filters.minPrice !== undefined && filters.maxPrice !== undefined && filters.minPrice > filters.maxPrice) {
      setError('Minimum rent cannot be greater than maximum rent');
      return;
    }
    setCurrentPage(0);
    loadProperties(filters, 0);
  }, [filters, loadProperties]);

  // ---- Handlers (stabilised with useCallback) ----
  const handleSearch = useCallback((location: string) => {
    const searchLocation = location?.trim() || 'Kuppam';
    const updatedFilters = {
      ...filters,
      location: searchLocation,
      displayLocation: searchLocation,
    };
    setFilters(updatedFilters);
    setCurrentPage(0);
    loadProperties(updatedFilters, 0);
  }, [filters, loadProperties]);

  const handleClear = useCallback(() => {
    const clearedFilters: Filters = {
      location: '',
      displayLocation: '',
      minPrice: undefined,
      maxPrice: undefined,
      bedrooms: undefined,
      propertyType: '',
      amenities: [],
      sortBy: filters.sortBy,
    };
    setFilters(clearedFilters);
    setCurrentPage(0);
    setActiveQuickChips([]);
    loadProperties(clearedFilters, 0);
  }, [filters.sortBy, loadProperties]);

  const handleReset = useCallback(() => {
    const resetFilters: Filters = {
      location: '',
      displayLocation: '',
      minPrice: undefined,
      maxPrice: undefined,
      bedrooms: undefined,
      propertyType: '',
      amenities: [],
      sortBy: 'newest',
    };

    setFilters(resetFilters);
    setActiveQuickChips([]);
    setCurrentPage(0);
    if (showMyListings) setShowMyListings(false);
    loadProperties(resetFilters, 0);
  }, [showMyListings, loadProperties]);

  const handleQuickChipClick = useCallback((chip: Chip) => {
    setActiveQuickChips(prev => {
      const isActive = prev.includes(chip.label);
      const newChips = isActive ? prev.filter(c => c !== chip.label) : [...prev, chip.label];

      const nextFilters: Filters = {
        ...filters,
        maxPrice: chip.filter.maxPrice ? (isActive ? undefined : chip.filter.maxPrice) : filters.maxPrice,
        bedrooms: chip.filter.bedrooms ? (isActive ? undefined : chip.filter.bedrooms) : filters.bedrooms,
        amenities: chip.filter.amenities ? (isActive ? [] : chip.filter.amenities) : filters.amenities,
      };

      setFilters(nextFilters);
      setCurrentPage(0);
      loadProperties(nextFilters, 0);

      return newChips;
    });
  }, [filters, loadProperties]);

  const handleVoiceSearch = useCallback(async (query: string) => {
    if (!query.trim()) return;

    setFilters(prev => ({ ...prev, displayLocation: query, location: query }));
    setSearching(true);
    setError(null);
    setAiExplanation(null);
    setProperties([]);
    setSearchProgress('🎤 Processing...');

    try {
      const response = await voiceSearch(query);
      const data = response.data;

      setSearchProgress('📊 Finding properties...');

      if (data.explanation) setAiExplanation(data.explanation);

      if (data.filters) {
        const extractedLocation = data.filters.location || '';
        setFilters(prev => ({
          ...prev,
          location: extractedLocation,
          displayLocation: extractedLocation || query,
          minPrice: data.filters.minRent ?? prev.minPrice,
          maxPrice: data.filters.maxRent ?? prev.maxPrice,
          bedrooms: data.filters.bedrooms ?? prev.bedrooms,
          amenities: data.filters.amenities ?? prev.amenities,
        }));
      }

      const resultCount = data.properties?.length || 0;
      setProperties(data.properties || []);
      setTotalPages(1);
      setSearchProgress(resultCount === 0 ? '😕 No properties found' : `✅ Found ${resultCount} properties`);
    } catch (err) {
      console.error('Voice search failed:', err);
      setError('AI search failed. Please try typing.');
      setProperties([]);
    } finally {
      setTimeout(() => {
        setSearching(false);
        setTimeout(() => setSearchProgress(''), 3000);
      }, 500);
    }
  }, []);

  // ---- Voice Search hook ----
  const {
    isListening: voiceListening,
    toggleListening: toggleVoiceListening,
    isSupported: speechSupported,
    error: speechRecognitionError,
  } = useVoiceSearch({
    onResult: (text) => {
      const normalized = text.trim();
      if (!normalized) return;
      setFilters(prev => ({ ...prev, displayLocation: normalized, location: normalized }));
      handleVoiceSearch(normalized);
    },
    onError: (message) => {
      setError(message);
      setSearching(false);
      setSearchProgress('');
    },
  });

  const toggleListening = useCallback(() => {
    toggleVoiceListening();
  }, [toggleVoiceListening]);

  // ---- fetch location info ----
  useEffect(() => {
    const fetchLocationInfo = async () => {
      try {
        const response = await fetch('/api/properties/location-info');
        const data = await response.json();
        setLocationInfo(data);
      } catch (error) {
        console.error('Failed to fetch location info:', error);
      }
    };
    fetchLocationInfo();
  }, []);

  // ---- Initial load ----
  useEffect(() => {
    loadProperties();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // ---- Reload on filter/sort change ----
  useEffect(() => {
    if (currentPage !== 0 || filters.sortBy !== 'newest' || showMyListings) {
      loadProperties();
    }
  }, [currentPage, filters.sortBy, showMyListings, loadProperties]);

  // ---- Memoised active filter count ----
  const activeFilterCount = useMemo(() => {
    return [
      filters.location,
      filters.minPrice,
      filters.maxPrice,
      filters.bedrooms,
      filters.propertyType,
      filters.amenities.length > 0,
    ].filter(v => v !== undefined && v !== '' && v !== null && v !== 0 && v !== false).length;
  }, [filters]);

  // ---- Memoised handlers for child components ----
  const handleFilterChange = useCallback((key: string, value: any) => {
    setFilters(f => ({ ...f, [key]: value }));
  }, []);

  const handleToggleFilters = useCallback(() => {
    setShowFilters(prev => !prev);
  }, []);

  const handleToggleMyListings = useCallback(() => {
    setShowMyListings(prev => !prev);
    setCurrentPage(0);
  }, []);

  const handleViewModeChange = useCallback((mode: 'list' | 'map') => {
    setViewMode(mode);
  }, []);

  const handlePropertyClick = useCallback((id: number) => {
    const property = properties.find(p => p.id === id);
    if (property) navigate(`/property/${property.id}`, { state: { property } });
  }, [properties, navigate]);

  // ---- Render ----
  if (loading && !searching) {
    return (
      <div className={styles.dashboard}>
        <div className={styles.loadingState}>
          <div className={styles.loadingSpinner}></div>
          <p>Finding your perfect home...</p>
        </div>
      </div>
    );
  }

  if (error && !searching) {
    return (
      <div className={styles.dashboard}>
        <div className={styles.errorState}>
          <div className={styles.errorIcon}>🔴</div>
          <h3>Oops! Something went wrong</h3>
          <p>{error}</p>
          <button onClick={() => loadProperties(filters, 0)} className={styles.retryBtn}>Try Again</button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.dashboard}>
      <HeroSection locationInfo={locationInfo} />
      
      <div className={styles.searchSection}>
        <SearchBar
          onSearch={handleSearch}
          onClear={handleClear}
          isListening={voiceListening}
          toggleListening={toggleListening}
          isSupported={speechSupported}
          voiceError={speechRecognitionError}
          showFilters={showFilters}
          onToggleFilters={handleToggleFilters}
          activeFilterCount={activeFilterCount}
        />
      </div>

      <div className={styles.chipsSection}>
        <QuickChips
          chips={quickChips}
          activeChips={activeQuickChips}
          onChipClick={handleQuickChipClick}
        />
      </div>

      {showFilters && (
        <div className={styles.filtersPanelWrapper}>
          <FiltersPanel
            filters={filters}
            onFilterChange={handleFilterChange}
            onApply={performSearch}
            onReset={handleReset}
          />
        </div>
      )}

      <div className={styles.resultsSection}>
        <ResultsHeader
          count={properties.length}
          aiExplanation={aiExplanation}
          viewMode={viewMode}
          onViewModeChange={handleViewModeChange}
          showMyListings={showMyListings}
          onToggleMyListings={handleToggleMyListings}
          isOwner={!!owner}
        />

        {searching && searchProgress && (
          <div className={styles.searchProgress}>
            <div className={styles.progressBar}><div className={styles.progressFill}></div></div>
            <p>{searchProgress}</p>
          </div>
        )}

        {viewMode === 'map' ? (
          <Suspense fallback={<div className={styles.loading}>Loading map...</div>}>
            <div className={styles.mapWrapper}>
              <PropertyMap
                properties={properties}
                center={mapCenter}
                zoom={mapZoom}
                onPropertyClick={handlePropertyClick}
              />
            </div>
          </Suspense>
        ) : (
          <ResultsList
            properties={properties}
            searching={searching}
            onPropertyClick={(p) => navigate(`/property/${p.id}`, { state: { property: p } })}
            onReset={handleReset}
          />
        )}

        {!showMyListings && totalPages > 1 && (
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={setCurrentPage}
          />
        )}
      </div>
    </div>
  );
};

export default Dashboard;