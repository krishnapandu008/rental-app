import React from 'react';
import styles from './QuickChips.module.scss';

// ✅ Define the Chip type properly
export interface Chip {
  label: string;
  filter: {
    maxPrice?: number;
    bedrooms?: number;
    amenities?: string[];
    [key: string]: any; // For flexibility
  };
  icon: string;
}

interface QuickChipsProps {
  chips: Chip[];
  activeChips: string[];
  onChipClick: (chip: Chip) => void;
}

const QuickChips: React.FC<QuickChipsProps> = ({ chips, activeChips, onChipClick }) => {
  if (chips.length === 0) return null;

  return (
    <div className={styles.chipsContainer}>
      <div className={styles.chipsScroll}>
        <div className={styles.chipsWrapper}>
          <span className={styles.chipsLabel}>Quick Filters</span>
          {chips.map((chip) => {
            const isActive = activeChips.includes(chip.label);

            return (
              <button
                key={chip.label}
                type="button"
                aria-pressed={isActive}
                className={`${styles.chip} ${isActive ? styles.active : ''}`}
                onClick={() => onChipClick(chip)}
              >
                <span className={styles.chipIcon}>{chip.icon}</span>
                <span className={styles.chipLabel}>{chip.label}</span>
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default QuickChips;