import React, { useState, useRef, useEffect } from 'react';
import styles from './SearchBar.module.scss';
import VoiceButton from '../../../../components/VoiceSearch/VoiceButton';
import { searchLocations } from '../../../../api/propertyApi';

interface SearchBarProps {
  onSearch: (location: string) => void;
  onClear: () => void;
  isListening: boolean;
  toggleListening: () => void;
  isSupported: boolean;
  voiceError: string | null;
  showFilters: boolean;
  onToggleFilters: () => void;
  activeFilterCount: number;
}

// Cache for AI suggestions to reduce API calls
const suggestionCache = new Map<string, string[]>();

// Get cached suggestions if available
const getCachedSuggestions = (query: string): string[] | null => {
  return suggestionCache.get(query) || null;
};

// Set cache
const setCachedSuggestions = (query: string, suggestions: string[]) => {
  suggestionCache.set(query, suggestions);
};

// AI-powered location suggestions via Ollama - No hardcoding!
const isAllowedLocation = (query: string) => {
  const normalized = query.trim().toLowerCase();
  return normalized.includes('kuppam');
};

const getAILocationSuggestions = async (query: string): Promise<string[]> => {
  console.log('🔍 getAILocationSuggestions called with:', query);
  
  if (!query || query.length < 1) {
    console.log('⚠️ Query too short, returning empty');
    return [];
  }

  // Check cache first
  const cached = getCachedSuggestions(query);
  if (cached) {
    console.log('📦 Cache hit for:', query, cached);
    return cached;
  }

  try {
    console.log('🌐 Calling API for:', query);
    const response = await searchLocations(query);
    console.log('📡 API Response:', response);
    
    if (response.data && response.data.length > 0) {
      const suggestions = response.data.slice(0, 8);
      console.log('✅ Suggestions received:', suggestions);
      setCachedSuggestions(query, suggestions);
      return suggestions;
    }

    console.log('ℹ️ No suggestions from API for:', query);
    return [];

  } catch (error) {
    console.error('❌ AI suggestion error:', error);
    return [];
  }
};

// Helper function to highlight matching text
const highlightMatch = (text: string, query: string): React.ReactNode => {
  if (!query || query.length === 0) return text;
  
  const lowerText = text.toLowerCase();
  const lowerQuery = query.toLowerCase().trim();
  const matchIndex = lowerText.indexOf(lowerQuery);
  
  if (matchIndex === -1) return text;
  
  const before = text.substring(0, matchIndex);
  const match = text.substring(matchIndex, matchIndex + query.length);
  const after = text.substring(matchIndex + query.length);
  
  return (
    <>
      {before}
      <span className={styles.highlightText}>{match}</span>
      {after}
    </>
  );
};

const SearchBar: React.FC<SearchBarProps> = ({
  onSearch,
  onClear,
  isListening,
  toggleListening,
  isSupported,
  voiceError,
  showFilters,
  onToggleFilters,
  activeFilterCount,
}) => {
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [selectedIndex, setSelectedIndex] = useState(-1);
  const [isLoading, setIsLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [showTooltip, setShowTooltip] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);
  const suggestionRef = useRef<HTMLDivElement>(null);
  const debounceTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const currentQueryRef = useRef<string>('');

  console.log('🔄 SearchBar render - searchQuery:', searchQuery, 'suggestions:', suggestions.length, 'showSuggestions:', showSuggestions);

  // Update suggestions when value changes - with debounce
  useEffect(() => {
    console.log('⚡ useEffect triggered with searchQuery:', searchQuery);
    
    // Clear previous timer
    if (debounceTimer.current) {
      clearTimeout(debounceTimer.current);
      debounceTimer.current = null;
    }

    const query = searchQuery.trim();
    currentQueryRef.current = query;

    console.log('📝 Query after trim:', query, 'length:', query.length);

    if (query && query.length >= 1) {
      if (!isAllowedLocation(query)) {
        console.log('❌ Unsupported location, hiding suggestions');
        setIsLoading(false);
        setSuggestions([]);
        setShowSuggestions(false);
        return;
      }

      console.log('✅ Query valid, fetching suggestions...');
      setIsLoading(true);
      
      debounceTimer.current = setTimeout(async () => {
        try {
          console.log('⏰ Debounce timer fired for:', query);
          
          // Check if query has changed
          if (currentQueryRef.current !== query) {
            console.log('⚠️ Query changed during fetch, aborting');
            return;
          }

          console.log('📞 Calling getAILocationSuggestions for:', query);
          const newSuggestions = await getAILocationSuggestions(query);
          console.log('📊 Suggestions received:', newSuggestions);
          
          // Double-check query hasn't changed while we were fetching
          if (currentQueryRef.current !== query) {
            console.log('⚠️ Query changed after fetch, aborting');
            return;
          }

          console.log('✅ Setting suggestions:', newSuggestions);
          setSuggestions(newSuggestions);
          setShowSuggestions(newSuggestions.length > 0);
          console.log('👀 Show suggestions set to:', newSuggestions.length > 0);
          setSelectedIndex(-1);
          setIsLoading(false);
          debounceTimer.current = null;
        } catch (error) {
          console.error('❌ Error fetching suggestions:', error);
          setIsLoading(false);
        }
      }, 300);
    } else {
      console.log('❌ Query too short or empty, clearing suggestions');
      setSuggestions([]);
      setShowSuggestions(false);
      setSelectedIndex(-1);
      setIsLoading(false);
    }

    return () => {
      console.log('🧹 Cleaning up useEffect for:', searchQuery);
      if (debounceTimer.current) {
        clearTimeout(debounceTimer.current);
        debounceTimer.current = null;
      }
    };
  }, [searchQuery]);

  // Close suggestions when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        suggestionRef.current && 
        !suggestionRef.current.contains(event.target as Node) &&
        inputRef.current &&
        !inputRef.current.contains(event.target as Node)
      ) {
        console.log('👆 Clicked outside, closing suggestions');
        setShowSuggestions(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  // Handle suggestion click
  const handleSuggestionClick = (suggestion: string) => {
    console.log('🖱️ Suggestion clicked:', suggestion);
    setSearchQuery(suggestion);
    setShowSuggestions(false);
    setSuggestions([]);
    if (inputRef.current) {
      inputRef.current.focus();
    }
    // ✅ Pass the suggestion as the location argument
    onSearch(suggestion);
  };

  // Handle keyboard navigation
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    console.log('⌨️ Key pressed:', e.key);
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setSelectedIndex(prev => (prev < suggestions.length - 1 ? prev + 1 : prev));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setSelectedIndex(prev => (prev > 0 ? prev - 1 : -1));
    } else if (e.key === 'Enter') {
      e.preventDefault();
      if (selectedIndex >= 0 && selectedIndex < suggestions.length) {
        handleSuggestionClick(suggestions[selectedIndex]);
      } else {
        console.log('🔍 Enter pressed, searching...');
        setShowSuggestions(false);
        // ✅ Pass the searchQuery or default 'Kuppam'
        onSearch(searchQuery.trim() || 'Kuppam');
      }
    } else if (e.key === 'Escape') {
      console.log('❌ Escape pressed, closing suggestions');
      setShowSuggestions(false);
    }
  };

  // Check if suggestions should be shown (for debugging)
  const shouldShowSuggestions = showSuggestions && suggestions.length > 0;
  console.log('🎨 Rendering - shouldShowSuggestions:', shouldShowSuggestions);

  return (
    <div className={styles.searchContainer}>
      <form
        className={styles.searchBar}
        onSubmit={(e) => {
          e.preventDefault();
          console.log('🔍 Form submitted');
          setShowSuggestions(false);
          // ✅ Pass the searchQuery or default 'Kuppam'
          onSearch(searchQuery.trim() || 'Kuppam');
        }}
      >
        {/* Location Icon */}
        <div className={styles.locationIcon}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
            <circle cx="12" cy="10" r="3" />
          </svg>
        </div>

        <div
          className={`${styles.searchInputWrapper} ${showTooltip ? styles.tooltipVisible : ''}`}
          onMouseEnter={() => setShowTooltip(true)}
          onMouseLeave={() => setShowTooltip(false)}
        >
          {/* Search Input - for property search (not location) */}
          <input
            ref={inputRef}
            type="text"
            placeholder="Search supports only in Kuppam, Andhra Pradesh"
            value={searchQuery}
            onChange={(e) => {
              console.log('✏️ Input changed:', e.target.value);
              setSearchQuery(e.target.value);
            }}
            onClick={() => setShowTooltip(false)}
            onKeyDown={handleKeyDown}
            onFocus={() => {
              console.log('🔍 Input focused, searchQuery:', searchQuery);
              if (searchQuery && searchQuery.length > 0 && suggestions.length > 0) {
                console.log('👀 Showing suggestions on focus');
                setShowSuggestions(true);
              }
            }}
            className={styles.searchInput}
            aria-label="Search supports only in Kuppam, Andhra Pradesh"
            aria-autocomplete="list"
            aria-expanded={showSuggestions}
            title="Search supports only in Kuppam, Andhra Pradesh"
          />
          <div className={styles.locationTooltip}>
            <span className={styles.locationTooltipIcon}>📍</span>
            <span className={styles.locationTooltipText}>Search supports only in Kuppam, Andhra Pradesh</span>
          </div>
          
          {isLoading && (
            <div className={styles.loadingIndicator}>
              <span className={styles.loadingDot}></span>
              <span className={styles.loadingDot}></span>
              <span className={styles.loadingDot}></span>
            </div>
          )}
          
          {searchQuery && !isLoading && (
            <button 
              type="button" 
              className={styles.clearBtn} 
              onClick={() => {
                console.log('🗑️ Clear button clicked');
                setSearchQuery('');
                onClear();
                setShowSuggestions(false);
                setSuggestions([]);
                setSelectedIndex(-1);
              }}
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          )}
          
          <div className={styles.divider}></div>
          <div className={styles.voiceWrapper}>
            <VoiceButton
              isListening={isListening}
              toggleListening={toggleListening}
              isSupported={isSupported}
              error={voiceError}
              size="md"
            />
          </div>
        </div>

        <div className={styles.searchActions}>
          <button 
            type="submit" 
            className={styles.searchBtn}
            onClick={() => setShowSuggestions(false)}
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
            <span>Search</span>
          </button>
          <button
            type="button"
            className={`${styles.filterBtn} ${showFilters ? styles.active : ''}`}
            onClick={onToggleFilters}
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="4" y1="21" x2="4" y2="14" />
              <line x1="4" y1="10" x2="4" y2="3" />
              <line x1="12" y1="21" x2="12" y2="12" />
              <line x1="12" y1="8" x2="12" y2="3" />
              <line x1="20" y1="21" x2="20" y2="16" />
              <line x1="20" y1="12" x2="20" y2="3" />
              <line x1="1" y1="14" x2="7" y2="14" />
              <line x1="9" y1="8" x2="15" y2="8" />
              <line x1="17" y1="16" x2="23" y2="16" />
            </svg>
            <span className={styles.filterLabel}>Filters</span>
            {activeFilterCount > 0 && (
              <span className={styles.filterBadge}>{activeFilterCount}</span>
            )}
          </button>
        </div>
      </form>

      {/* AI-Powered Location Suggestions - Only from Ollama */}
      {shouldShowSuggestions && (
        <div className={styles.suggestions} ref={suggestionRef}>
          <div className={styles.suggestionsHeader}>
            <span className={styles.aiBadge}>🤖 AI</span>
            <span>Suggestions for "{searchQuery}"</span>
          </div>
          {suggestions.map((suggestion, index) => {
            const highlightedText = highlightMatch(suggestion, searchQuery);
            
            return (
              <div 
                key={index}
                className={`${styles.suggestionItem} ${selectedIndex === index ? styles.selected : ''}`}
                onClick={() => handleSuggestionClick(suggestion)}
                onMouseEnter={() => {
                  console.log('🖱️ Hover on suggestion:', suggestion);
                  setSelectedIndex(index);
                }}
              >
                <span className={styles.suggestionIcon}>📍</span>
                <span className={styles.suggestionText}>
                  {highlightedText}
                </span>
                <span className={styles.suggestionArrow}>→</span>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default SearchBar;