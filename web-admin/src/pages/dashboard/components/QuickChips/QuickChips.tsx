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
  return (
    <div className={styles.quickChipsContainer}>
      {chips.map((chip) => {
        const isActive = activeChips.includes(chip.label);

        return (
          <button
            key={chip.label}
            type="button"
            aria-pressed={isActive}
            className={`${styles.quickChip} ${isActive ? styles.activeChip : ''}`}
            onClick={() => onChipClick(chip)}
          >
            {chip.icon} {chip.label}
          </button>
        );
      })}
    </div>
  );
};

export default QuickChips;