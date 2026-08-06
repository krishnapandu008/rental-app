import React, { useState, useEffect, useCallback, lazy, Suspense } from 'react';
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

// ✅ Lazy load map
const PropertyMap = lazy(() => import('../../components/PropertyMap'));

// ✅ Types
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

// ✅ Quick chips data with proper Chip type
const quickChips: Chip[] = [
  { label: 'Under ₹10,000', filter: { maxPrice: 10000 }, icon: '💰' },
  { label: 'Under ₹15,000', filter: { maxPrice: 15000 }, icon: '💰' },
  { label: '1 BHK', filter: { bedrooms: 1 }, icon: '🛏️' },
  { label: '2 BHK', filter: { bedrooms: 2 }, icon: '🛏️' },
  { label: '3 BHK', filter: { bedrooms: 3 }, icon: '🛏️' },
  { label: 'With Parking', filter: { amenities: ['parking'] }, icon: '🅿️' },
  { label: 'Furnished', filter: { amenities: ['furnished'] }, icon: '🛋️' },
];

const Dashboard: React.FC = () => {
  const { owner } = useAuth();
  const navigate = useNavigate();

  // 📊 State
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

  // 🔍 Filters
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

  // ✅ Fixed: Use constants instead of state
  const mapZoom = 10;

  // 🎤 Voice Search State
  const {
    isListening: voiceListening,
    transcript: voiceTranscript,
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

  const isSupported = speechSupported;
  const isListening = voiceListening;
  const voiceError = speechRecognitionError;

  // 🔄 Effects
  useEffect(() => {
    if (voiceTranscript && !voiceListening && voiceTranscript.trim()) {
      handleVoiceSearch(voiceTranscript);
    }
  }, [voiceTranscript, voiceListening]);

  useEffect(() => {
    loadProperties();
  }, []);

  useEffect(() => {
    if (currentPage !== 0 || filters.sortBy !== 'newest' || showMyListings) {
      loadProperties();
    }
  }, [currentPage, filters.sortBy, showMyListings]);

  // 📊 Active filter count
  const activeFilterCount = [
    filters.location,
    filters.minPrice,
    filters.maxPrice,
    filters.bedrooms,
    filters.propertyType,
    filters.amenities.length > 0,
  ].filter(v => v !== undefined && v !== '' && v !== null && v !== 0 && v !== false).length;

  const overviewCards = [
    {
      title: 'Live matches',
      value: `${properties.length}`,
      caption: filters.displayLocation || filters.location ? 'Fresh results for your search' : 'Updated instantly as you filter',
    },
    {
      title: 'AI search',
      value: 'On',
      caption: aiExplanation ? 'Insights are helping refine results' : 'Voice and text search ready',
    },
    {
      title: 'Listings mode',
      value: showMyListings ? 'Mine' : 'All',
      caption: owner ? 'Switch between your portfolio and public listings' : 'Browse public rental inventory',
    },
    {
      title: 'Active filters',
      value: `${activeFilterCount}`,
      caption: activeFilterCount > 0 ? 'Refined to your ideal rental profile' : 'No filters applied yet',
    },
  ];

  // ================================================================
  // 🎯 HANDLERS
  // ================================================================

  const handleVoiceSearch = async (query: string) => {
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
  };

  const loadProperties = useCallback(async (requestedFilters: Filters = filters, page = currentPage) => {
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
          location: requestedFilters.location || undefined,
          minPrice: requestedFilters.minPrice,
          maxPrice: requestedFilters.maxPrice,
          bedrooms: requestedFilters.bedrooms,
          propertyType: requestedFilters.propertyType || undefined,
          amenities: requestedFilters.amenities?.length ? requestedFilters.amenities : undefined,
          sortBy: requestedFilters.sortBy,
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

  const performSearch = (page = 0) => {
    setCurrentPage(page);
    loadProperties(filters, page);
  };

  const handleQuickChipClick = (chip: Chip) => {
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
  };

  const handleReset = () => {
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
  };

  const toggleListening = () => {
    toggleVoiceListening();
  };

  // ================================================================
  // 🖥️ RENDER
  // ================================================================

  if (loading && !searching) {
    return (
      <div className={styles.dashboard}>
        <div className={styles.loadingState}>
          <div className={styles.loadingSpinner}></div>
          <p>Loading properties...</p>
        </div>
      </div>
    );
  }

  if (error && !searching) {
    return (
      <div className={styles.dashboard}>
        <div className={styles.errorState}>
          <div className={styles.errorIcon}>🔴</div>
          <h3>Something went wrong</h3>
          <p>{error}</p>
          <button onClick={() => loadProperties(filters, 0)} className={styles.retryBtn}>Try Again</button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.dashboard}>
      {/* ✅ Hero Section */}
      <HeroSection />

      <div className={styles.dashboardOverview}>
        <div className={styles.overviewPrimary}>
          <span className={styles.overviewBadge}>Advanced rental experience</span>
          <h3>Smart browsing, curated matches, and a premium search flow.</h3>
          <p>
            {filters.displayLocation || filters.location
              ? `Showing tailored opportunities for ${filters.displayLocation || filters.location}.`
              : 'Explore verified rentals with AI-powered recommendations and refined filters.'}
          </p>
        </div>
        <div className={styles.overviewCards}>
          {overviewCards.map((card) => (
            <div className={styles.overviewCard} key={card.title}>
              <span className={styles.overviewCardTitle}>{card.title}</span>
              <strong>{card.value}</strong>
              <p>{card.caption}</p>
            </div>
          ))}
        </div>
      </div>

      {/* ✅ Search Bar */}
      <SearchBar
        value={filters.displayLocation || filters.location}
        onChange={(value) => setFilters(f => ({ ...f, displayLocation: value, location: value }))}
        onSearch={performSearch}
        onClear={() => {
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
        }}
        isListening={isListening}
        toggleListening={toggleListening}
        isSupported={isSupported}
        voiceError={voiceError}
        showFilters={showFilters}
        onToggleFilters={() => setShowFilters(!showFilters)}
        activeFilterCount={activeFilterCount}
      />

      {/* ✅ Quick Chips */}
      <QuickChips
        chips={quickChips}
        activeChips={activeQuickChips}
        onChipClick={handleQuickChipClick}
      />

      {/* ✅ Filters Panel */}
      {showFilters && (
        <FiltersPanel
          filters={filters}
          onFilterChange={(key, value) => setFilters(f => ({ ...f, [key]: value }))}
          onApply={performSearch}
          onReset={handleReset}
        />
      )}

      {/* ✅ Results Header */}
      <ResultsHeader
        count={properties.length}
        aiExplanation={aiExplanation}
        viewMode={viewMode}
        onViewModeChange={setViewMode}
        showMyListings={showMyListings}
        onToggleMyListings={() => {
          setShowMyListings(!showMyListings);
          setCurrentPage(0);
        }}
        isOwner={!!owner}
      />

      {/* ✅ Voice Search Progress */}
      {searching && searchProgress && (
        <div className={styles.searchProgress}>
          <div className={styles.progressBar}><div className={styles.progressFill}></div></div>
          <p>{searchProgress}</p>
        </div>
      )}

      {/* ✅ Results Area */}
      {viewMode === 'map' ? (
        <Suspense fallback={<div className={styles.loading}>Loading map...</div>}>
          <PropertyMap
            properties={properties}
            center={mapCenter}
            zoom={mapZoom}
            onPropertyClick={(id: number) => {
              const property = properties.find(p => p.id === id);
              if (property) navigate(`/property/${property.id}`, { state: { property } });
            }}
          />
        </Suspense>
      ) : (
        <ResultsList
          properties={properties}
          searching={searching}
          onPropertyClick={(p) => navigate(`/property/${p.id}`, { state: { property: p } })}
          onReset={handleReset}
        />
      )}

      {/* ✅ Pagination */}
      {!showMyListings && totalPages > 1 && (
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={setCurrentPage}
        />
      )}
    </div>
  );
};

export default Dashboard;