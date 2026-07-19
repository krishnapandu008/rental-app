import React from 'react';
import { Property } from '../../types';
import { formatCurrency } from '../../utils/helpers';
import styles from './PropertyCard.module.scss';

interface Props {
  property: Property;
  onOpen: (property: Property) => void;
}

const PropertyCard: React.FC<Props> = ({ property, onOpen }) => {
  const imageUrl = property.imageUrls && property.imageUrls.length > 0 
    ? property.imageUrls[0] 
    : null;

  const imageCount = property.imageUrls?.length || 0;

  return (
    <div className={styles.card} onClick={() => onOpen(property)}>
      {/* Image Section */}
      <div className={styles.imageWrapper}>
        {imageUrl ? (
          <>
            <img 
              src={imageUrl} 
              alt={property.title} 
              className={styles.image}
            />
            {imageCount > 1 && (
              <span className={styles.imageCount}>
                +{imageCount - 1} more
              </span>
            )}
          </>
        ) : (
          <div className={styles.imagePlaceholder} />
        )}
        
        {/* Availability Badge */}
        {property.available && (
          <span className={styles.availabilityBadge}>Available</span>
        )}
        
        {/* Visibility Badge */}
        <span className={`${styles.visibilityBadge} ${styles[property.visibility?.toLowerCase() || 'public']}`}>
          {property.visibility || 'PUBLIC'}
        </span>

        {/* Inactive Overlay */}
        {!property.isActive && (
          <div className={styles.inactiveOverlay}>
            <span>Inactive</span>
          </div>
        )}
      </div>

      {/* Content Section */}
      <div className={styles.content}>
        <div className={styles.header}>
          <h3>{property.title}</h3>
          <span className={styles.rent}>{formatCurrency(property.rent)}</span>
        </div>
        
        <div className={styles.location}>
          {property.location}
        </div>
        
        <div className={styles.details}>
          <span>🛏️ {property.bedrooms} BHK</span>
          {property.description && (
            <span>📝 {property.description.length > 30 
              ? property.description.slice(0, 30) + '...' 
              : property.description}
            </span>
          )}
        </div>
        
        <div className={styles.contact}>
          📞 {property.contactNumber}
        </div>
      </div>
    </div>
  );
};

export default PropertyCard;