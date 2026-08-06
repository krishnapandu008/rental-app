import React from 'react';
import styles from './AmenityBadges.module.scss';

interface Props {
  amenities?: string[];
  className?: string;
}

const AmenityBadges: React.FC<Props> = ({ amenities = [], className = '' }) => {
  if (!amenities || amenities.length === 0) return null;

  return (
    <div className={`${styles.wrap} ${className}`}>
      {amenities.includes('parking') && (
        <span className={`${styles.badge} ${styles.parking}`}>🅿️ Parking</span>
      )}
      {amenities.includes('furnished') && (
        <span className={`${styles.badge} ${styles.furnished}`}>🛋️ Furnished</span>
      )}
    </div>
  );
};

export default AmenityBadges;
