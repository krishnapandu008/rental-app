import React from 'react';
import styles from './AmenitiesCheckboxes.module.scss';

interface Props {
  value?: string[];
  onChange: (next: string[]) => void;
}

const AmenitiesCheckboxes: React.FC<Props> = ({ value = [], onChange }) => {
  const toggle = (amenity: string) => {
    const current = value || [];
    const next = current.includes(amenity) ? current.filter(a => a !== amenity) : [...current, amenity];
    onChange(next);
  };

  return (
    <div className={styles.row}>
      <label className={styles.label}>
        <input type="checkbox" checked={value.includes('parking')} onChange={() => toggle('parking')} /> Parking
      </label>
      <label className={styles.label}>
        <input type="checkbox" checked={value.includes('furnished')} onChange={() => toggle('furnished')} /> Furnished
      </label>
    </div>
  );
};

export default AmenitiesCheckboxes;
