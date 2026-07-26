import React, { useState, useEffect } from 'react';
import { Property } from '../../types';
import { formatCurrency } from '../../utils/helpers';
import { toggleFavorite } from '../../api/propertyApi';
import { useAuth } from '../../contexts/AuthContext';
import styles from './PropertyCard.module.scss';

interface Props {
  property: Property;
  onOpen: (property: Property) => void;
}

const PropertyCard: React.FC<Props> = ({ property, onOpen }) => {
  const { owner } = useAuth();
  const [favorited, setFavorited] = useState(property.isFavorited || false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setFavorited(property.isFavorited || false);
  }, [property.isFavorited]);

  const handleFavoriteToggle = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (!owner) {
      alert('Please login to save favorites');
      return;
    }
    if (loading) return;
    setLoading(true);
    try {
      const response = await toggleFavorite(property.id);
      setFavorited(response.data);
    } catch (err) {
      console.error(err);
      alert('Failed to update favorite');
    } finally {
      setLoading(false);
    }
  };

  const imageUrl = property.imageUrls && property.imageUrls.length > 0 
    ? property.imageUrls[0] 
    : null;

  const imageCount = property.imageUrls?.length || 0;

  return (
    <div className={styles.card} onClick={() => onOpen(property)}>
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
        
        {property.available && (
          <span className={styles.availabilityBadge}>Available</span>
        )}
        
        <span className={`${styles.visibilityBadge} ${styles[property.visibility?.toLowerCase() || 'public']}`}>
          {property.visibility || 'PUBLIC'}
        </span>

        {!property.isActive && (
          <div className={styles.inactiveOverlay}>
            <span>Inactive</span>
          </div>
        )}

        <button
          className={`${styles.favoriteBtn} ${favorited ? styles.favorited : ''}`}
          onClick={handleFavoriteToggle}
          disabled={loading}
          aria-label={favorited ? 'Remove from favorites' : 'Add to favorites'}
        >
          <svg
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill={favorited ? 'currentColor' : 'none'}
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
          </svg>
        </button>
      </div>

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