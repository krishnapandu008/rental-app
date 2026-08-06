import React from 'react';
// ✅ Correct paths
import styles from './ResultsList.module.scss';
import { Property } from '../../../../types';
import PropertyCard from '../../../../components/PropertyCard/PropertyCard';

interface ResultsListProps {
  properties: Property[];
  searching: boolean;
  onPropertyClick: (property: Property) => void;
  onReset: () => void;
}

const ResultsList: React.FC<ResultsListProps> = ({
  properties,
  searching,
  onPropertyClick,
  onReset,
}) => {
  if (properties.length === 0 && !searching) {
    return (
      <div className={styles.emptyState}>
        <div className={styles.emptyIcon}>🏠</div>
        <h3>No properties found</h3>
        <p>Try adjusting your filters or search query</p>
        <button className={styles.clearFiltersBtn} onClick={onReset}>Clear Filters</button>
      </div>
    );
  }

  return (
    <div className={styles.propertyGrid}>
      {properties.map((prop) => (
        <PropertyCard
          key={prop.id}
          property={prop}
          onOpen={onPropertyClick}
        />
      ))}
    </div>
  );
};

export default ResultsList;