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

  const amenities = [
    { id: 'parking', label: '🅿️ Parking' },
    { id: 'furnished', label: '🛋️ Furnished' },
    { id: 'ac', label: '❄️ AC' },
    { id: 'security', label: '🔒 Security' },
    { id: 'gym', label: '💪 Gym' },
    { id: 'swimming_pool', label: '🏊 Pool' },
    { id: 'garden', label: '🌿 Garden' },
    { id: 'wifi', label: '📶 WiFi' },
    { id: 'pet_friendly', label: '🐾 Pet Friendly' },
    { id: 'water_supply', label: '💧 Water Supply' },
    { id: 'power_backup', label: '⚡ Power Backup' },
    { id: 'lift', label: '🛗 Lift' },
  ];

  return (
    <div className={styles.amenitiesContainer}>
      {amenities.map((amenity) => {
        const isChecked = value.includes(amenity.id);
        return (
          <label
            key={amenity.id}
            className={`${styles.amenityCheckbox} ${isChecked ? styles.checked : ''}`}
          >
            <input
              type="checkbox"
              checked={isChecked}
              onChange={() => toggle(amenity.id)}
            />
            <span>{amenity.label}</span>
          </label>
        );
      })}
    </div>
  );
};

export default AmenitiesCheckboxes;