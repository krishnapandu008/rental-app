import React from 'react';
import styles from './ResultsHeader.module.scss';

interface ResultsHeaderProps {
  count: number;
  aiExplanation: string | null;
  viewMode: 'list' | 'map';
  onViewModeChange: (mode: 'list' | 'map') => void;
  showMyListings: boolean;
  onToggleMyListings: () => void;
  isOwner: boolean;
}

const ResultsHeader: React.FC<ResultsHeaderProps> = ({
  count,
  aiExplanation,
  viewMode,
  onViewModeChange,
  showMyListings,
  onToggleMyListings,
  isOwner,
}) => {
  return (
    <div className={styles.resultsHeader}>
      <div className={styles.resultsLeft}>
        <h2>{count} {count === 1 ? 'Property' : 'Properties'} Found</h2>
        {aiExplanation && <span className={styles.aiTag}>🤖 {aiExplanation}</span>}
      </div>
      <div className={styles.resultsRight}>
        <div className={styles.viewToggle}>
          {['list', 'map'].map((mode) => (
            <button
              key={mode}
              className={`${styles.viewBtn} ${viewMode === mode ? styles.activeView : ''}`}
              onClick={() => onViewModeChange(mode as 'list' | 'map')}
            >
              {mode === 'list' ? '📋 List' : '🗺️ Map'}
            </button>
          ))}
        </div>
        {isOwner && (
          <button className={styles.myListingsBtn} onClick={onToggleMyListings}>
            {showMyListings ? '📤 All Properties' : '📥 My Listings'}
          </button>
        )}
      </div>
    </div>
  );
};

export default ResultsHeader;