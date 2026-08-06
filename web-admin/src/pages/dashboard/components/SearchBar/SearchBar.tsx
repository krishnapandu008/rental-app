import React from 'react';
// ✅ Correct path to your existing VoiceButton
import styles from './SearchBar.module.scss';
import VoiceButton from '../../../../components/VoiceSearch/VoiceButton';

interface SearchBarProps {
  value: string;
  onChange: (value: string) => void;
  onSearch: () => void;
  onClear: () => void;
  isListening: boolean;
  toggleListening: () => void;
  isSupported: boolean;
  voiceError: string | null;
  showFilters: boolean;
  onToggleFilters: () => void;
  activeFilterCount: number;
}

const SearchBar: React.FC<SearchBarProps> = ({
  value,
  onChange,
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
  return (
    <form
      className={styles.searchBar}
      onSubmit={(e) => {
        e.preventDefault();
        onSearch();
      }}
    >
      <div className={styles.searchInputWrapper}>
        <span className={styles.searchIcon}>🔍</span>
        <input
          type="text"
          placeholder="Search by location, city, or property..."
          value={value}
          onChange={(e) => onChange(e.target.value)}
          className={styles.searchInput}
          aria-label="Search properties"
        />
        <VoiceButton
          isListening={isListening}
          toggleListening={toggleListening}
          isSupported={isSupported}
          error={voiceError}
          size="md"
        />
        {value && (
          <button type="button" className={styles.clearSearchBtn} onClick={onClear}>
            ✕
          </button>
        )}
      </div>
      <button type="submit" className={styles.searchBtn}>
        Search
      </button>
      <button
        type="button"
        className={`${styles.filterToggleBtn} ${showFilters ? styles.active : ''}`}
        onClick={onToggleFilters}
      >
        <span>⚙️</span>
        {activeFilterCount > 0 && <span className={styles.filterBadge}>{activeFilterCount}</span>}
      </button>
    </form>
  );
};

export default SearchBar;