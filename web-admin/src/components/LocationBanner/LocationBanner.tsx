import React from 'react';
import styles from './LocationBanner.module.scss';

interface LocationBannerProps {
  location: string;
  district: string;
  state: string;
  pinCode: string;
}

const LocationBanner: React.FC<LocationBannerProps> = ({
  location,
  district,
  state,
  pinCode,
}) => {
  return (
    <div className={styles.banner}>
      <div className={styles.bannerContent}>
        <span className={styles.bannerIcon}>📍</span>
        <div className={styles.bannerText}>
          <span className={styles.bannerTitle}>
            Serving {location}, {district} District, {state}
          </span>
          <span className={styles.bannerSubtitle}>PIN: {pinCode}</span>
        </div>
      </div>
    </div>
  );
};

export default LocationBanner;