import React, { useState, useEffect } from 'react';
import { Property } from '../../types';
import { formatCurrency } from '../../utils/helpers';
import { toggleFavorite } from '../../api/propertyApi';
import { useAuth } from '../../contexts/AuthContext';
import styles from './PropertyCard.module.scss';
import AmenityBadges from '../AmenityBadges/AmenityBadges';

interface Props {
  property: Property;
  onOpen: (property: Property) => void;
  onFavoriteToggle?: (propertyId: number) => void;
}

const PropertyCard: React.FC<Props> = ({ property, onOpen, onFavoriteToggle }) => {
  const { owner } = useAuth();
  const runtimeProperty = property as Property & {
    imageUrls?: string[];
    location?: Property['location'] | string | null;
    amenities?: Property['amenities'] | string[] | null;
  };
  const [favorited, setFavorited] = useState(property.favorited || false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setFavorited(property.favorited || false);
  }, [property.favorited]);

  const handleFavoriteToggle = async (e: React.MouseEvent) => {
    e.stopPropagation();
    e.preventDefault();
    
    if (!owner) {
      alert('Please login to save favorites');
      return;
    }
    
    if (loading) return;
    setLoading(true);
    
    const previousState = favorited;
    
    try {
      const response = await toggleFavorite(property.id);
      const newState = response.data;
      setFavorited(newState);
      
      if (onFavoriteToggle && !newState) {
        onFavoriteToggle(property.id);
      }
    } catch (err) {
      console.error('Failed to toggle favorite:', err);
      setFavorited(previousState);
      alert('Failed to update favorite. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const imageUrls = Array.isArray(property.images) && property.images.length > 0
    ? property.images.map((image) => image.imageUrl)
    : runtimeProperty.imageUrls || [];
  const imageUrl = imageUrls[0] || null;
  const imageCount = imageUrls.length;
  const locationLabel = typeof runtimeProperty.location === 'string'
    ? runtimeProperty.location
    : runtimeProperty.location?.displayName || 'Location unavailable';
  const amenityNames = Array.isArray(runtimeProperty.amenities)
    ? runtimeProperty.amenities.map((amenity) => typeof amenity === 'string' ? amenity : amenity.amenityName)
    : [];

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
            width="22"
            height="22"
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
          <h3 className={styles.title}>{property.title}</h3>
          <span className={styles.rent}>{formatCurrency(property.rent)}</span>
        </div>
        
        <div className={styles.location}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
            <circle cx="12" cy="10" r="3" />
          </svg>
          <span>{locationLabel}</span>
        </div>
        
        <div className={styles.details}>
          <span className={styles.detailItem}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
              <polyline points="9 22 9 12 15 12 15 22" />
            </svg>
            {property.bedrooms} BHK
          </span>
          {property.description && (
            <span className={styles.detailItem}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
              </svg>
              {property.description.length > 30 
                ? property.description.slice(0, 30) + '...' 
                : property.description}
            </span>
          )}
        </div>
        
        <div className={styles.contact}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z" />
          </svg>
          {property.contactNumber}
        </div>
              <AmenityBadges amenities={amenityNames} />
            </div>
    </div>
  );
};

export default PropertyCard;