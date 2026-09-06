import React from 'react';
import styles from './FiltersPanel.module.scss';
import AmenitiesCheckboxes from '../../../../components/AmenitiesCheckboxes/AmenitiesCheckboxes';

interface Filters {
  minPrice: number | undefined;
  maxPrice: number | undefined;
  bedrooms: number | undefined;
  propertyType: string;
  sortBy: string;
  amenities?: string[];
}

interface FiltersPanelProps {
  filters: Filters;
  onFilterChange: (key: string, value: any) => void;
  onApply: () => void;
  onReset: () => void;
}

const FiltersPanel: React.FC<FiltersPanelProps> = ({
  filters,
  onFilterChange,
  onApply,
  onReset,
}) => {
  const filterConfigs = [
    { label: 'Min Price', key: 'minPrice', placeholder: '₹ Min', type: 'number' },
    { label: 'Max Price', key: 'maxPrice', placeholder: '₹ Max', type: 'number' },
  ];

  const handleNumericInput = (key: string, value: string) => {
    if (value === '') {
      onFilterChange(key, undefined);
      return;
    }

    const numericValue = Number(value);
    if (!Number.isNaN(numericValue) && numericValue >= 0) {
      onFilterChange(key, numericValue);
    }
  };

  return (
    <div className={styles.filtersPanel}>
      <div className={styles.filtersGrid}>
        {filterConfigs.map(({ label, key, placeholder, type }) => (
          <div className={styles.filterGroup} key={key}>
            <label htmlFor={`filter-${key}`}>{label}</label>
            <input
              id={`filter-${key}`}
              type={type}
              inputMode="numeric"
              min="0"
              step="1000"
              placeholder={placeholder}
              value={filters[key as keyof Filters] ?? ''}
              onChange={(e) => handleNumericInput(key, e.target.value)}
            />
          </div>
        ))}

        <div className={styles.filterGroup}>
          <label htmlFor="filter-bedrooms">Bedrooms</label>
          <select
            id="filter-bedrooms"
            value={filters.bedrooms ?? ''}
            onChange={(e) => onFilterChange('bedrooms', e.target.value ? Number(e.target.value) : undefined)}
          >
            <option value="">All</option>
            {[1, 2, 3, 4].map(b => <option key={b} value={b}>{b} BHK</option>)}
          </select>
        </div>

        <div className={styles.filterGroup}>
          <label htmlFor="filter-propertyType">Property Type</label>
          <select
            id="filter-propertyType"
            value={filters.propertyType}
            onChange={(e) => onFilterChange('propertyType', e.target.value)}
          >
            <option value="">All Types</option>
            {['apartment', 'villa', 'studio', 'house'].map(t => (
              <option key={t} value={t}>{t.charAt(0).toUpperCase() + t.slice(1)}</option>
            ))}
          </select>
        </div>

        <div className={styles.filterGroup}>
          <label htmlFor="filter-sortBy">Sort By</label>
          <select
            id="filter-sortBy"
            value={filters.sortBy}
            onChange={(e) => onFilterChange('sortBy', e.target.value)}
          >
            <option value="newest">Newest First</option>
            <option value="price_asc">Price: Low to High</option>
            <option value="price_desc">Price: High to Low</option>
          </select>
        </div>
        
        <div className={`${styles.filterGroup} ${styles.fullWidth}`}>
          <label>Amenities</label>
          <div className={styles.amenitiesWrapper}>
            <AmenitiesCheckboxes 
              value={filters.amenities} 
              onChange={(next) => onFilterChange('amenities', next)} 
            />
          </div>
        </div>
      </div>

      <div className={styles.filtersActions}>
        <button className={styles.applyFiltersBtn} onClick={() => onApply()}>
          Apply Filters
        </button>
        <button className={styles.resetFiltersBtn} onClick={onReset}>
          Reset All
        </button>
      </div>
    </div>
  );
};

export default FiltersPanel;