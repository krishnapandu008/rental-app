import React from 'react';
import { Property } from '../../types';
import { formatCurrency } from '../../utils/helpers';
import styles from './PropertyCard.module.scss';

interface Props {
  property: Property;
  onDelete: (id: number) => void;
}

const PropertyCard: React.FC<Props> = ({ property, onDelete }) => {
  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <h3>{property.title}</h3>
        <span className={styles.rent}>{formatCurrency(property.rent)}/month</span>
      </div>
      <p className={styles.location}>{property.location}</p>
      <p className={styles.bedrooms}>{property.bedrooms} BHK</p>
      <p className={styles.contact}>📞 {property.contactNumber}</p>
      <button onClick={() => onDelete(property.id)} className={styles.deleteBtn}>Delete</button>
    </div>
  );
};

export default PropertyCard;