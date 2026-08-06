import React from 'react';
import styles from './HeroSection.module.scss';

const HeroSection: React.FC = () => {
  return (
    <div className={styles.heroSection}>
      <div className={styles.heroContent}>
        <span className={styles.heroEyebrow}>AI-powered rental discovery</span>
        <h1>Find the right rental faster with confidence.</h1>
        <p>
          Browse verified homes, compare neighborhoods, and discover properties that fit your budget,
          commute, and lifestyle in one elegant experience.
        </p>
        <div className={styles.heroActions}>
          <button className={styles.primaryBtn}>Explore listings</button>
          <button className={styles.secondaryBtn}>View featured homes</button>
        </div>
        <div className={styles.heroBadges}>
          <span>Verified homes</span>
          <span>Instant filters</span>
          <span>Flexible leases</span>
        </div>
      </div>

      <div className={styles.heroPanel}>
        <div className={styles.heroPanelCard}>
          <span className={styles.heroPanelLabel}>Live demand</span>
          <strong>+24%</strong>
          <p>Higher interest in premium neighborhoods this week.</p>
        </div>
        <div className={styles.heroPanelCard}>
          <span className={styles.heroPanelLabel}>Fast approvals</span>
          <strong>2 min</strong>
          <p>Average time to shortlist a property match.</p>
        </div>
      </div>
    </div>
  );
};

export default HeroSection;