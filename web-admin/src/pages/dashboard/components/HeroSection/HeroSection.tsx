import React from 'react';
import styles from './HeroSection.module.scss';

interface HeroSectionProps {
  locationInfo?: {
    location: string;
    district: string;
    state: string;
    pinCode: string;
  } | null;
}

const HeroSection: React.FC<HeroSectionProps> = ({ locationInfo }) => {
  const locationLabel = locationInfo?.location || 'your area';

  return (
    <div className={styles.heroSection}>
      <div className={styles.heroContent}>
        <div className={styles.heroBadge}>
          <span className={styles.badgePulse}></span>
          🏠 Find Your Dream Rental
        </div>
        <h1>
          Discover <span className={styles.highlight}>Perfect Homes</span>
          <br />With AI-Powered Search
        </h1>
        <p>
          Search, compare, and book verified rentals in {locationLabel} with smart filters,
          voice search, and instant inquiries - all in one place.
        </p>

        <div className={styles.featureGrid}>
          <div className={styles.featureItem}>
            <span className={styles.featureIcon}>🎤</span>
            <div>
              <strong>Voice Search</strong>
              <span>Find properties with AI</span>
            </div>
          </div>
          <div className={styles.featureItem}>
            <span className={styles.featureIcon}>🔍</span>
            <div>
              <strong>Smart Filters</strong>
              <span>Price • Location • Amenities</span>
            </div>
          </div>
          <div className={styles.featureItem}>
            <span className={styles.featureIcon}>❤️</span>
            <div>
              <strong>Favorites</strong>
              <span>Save & track listings</span>
            </div>
          </div>
          <div className={styles.featureItem}>
            <span className={styles.featureIcon}>🗺️</span>
            <div>
              <strong>Map View</strong>
              <span>Visual property search</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default HeroSection;