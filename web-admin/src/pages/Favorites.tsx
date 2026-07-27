import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { getFavoriteIds, getProperties } from '../api/propertyApi';
import { Property } from '../types';
import PropertyCard from '../components/PropertyCard/PropertyCard';
import styles from './Favorites.module.scss';

const Favorites: React.FC = () => {
  const navigate = useNavigate();
  const [favorites, setFavorites] = useState<Property[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadFavorites = useCallback(async () => {
    try {
      setLoading(true);
      setError('');

      const idsRes = await getFavoriteIds();
      const ids = idsRes.data || [];
      
      if (ids.length === 0) {
        setFavorites([]);
        setLoading(false);
        return;
      }

      const propsRes = await getProperties({ size: 100 });
      const allProperties = propsRes.data?.content || [];
      const favoriteProps = allProperties.filter(p => ids.includes(p.id));
      
      const favoritesWithFlag = favoriteProps.map(p => ({ ...p, isFavorited: true }));
      setFavorites(favoritesWithFlag);
    } catch (err: any) {
      console.error('Error loading favorites:', err);
      setError(err.message || 'Failed to load favorites');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadFavorites();
  }, [loadFavorites]);

  const handleRemoveFavorite = useCallback(async (propertyId: number) => {
    await loadFavorites();
  }, [loadFavorites]);

  const handleOpen = (property: Property) => {
    navigate(`/property/${property.id}`, { state: { property } });
  };

  if (loading) return <div className={styles.loading}>Loading favorites...</div>;
  if (error) return <div className={styles.error}>Error: {error}</div>;

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2 className={styles.title}>❤️ My Favorites</h2>
        <span className={styles.count}>{favorites.length} properties</span>
      </div>
      
      {favorites.length === 0 ? (
        <div className={styles.emptyState}>
          <div className={styles.emptyIcon}>❤️</div>
          <h3>No favorites yet</h3>
          <p>Start saving properties you love by clicking the heart icon.</p>
          <a href="/" className={styles.browseBtn}>Browse properties →</a>
        </div>
      ) : (
        <div className={styles.propertyGrid}>
          {favorites.map((prop) => (
            <PropertyCard
              key={prop.id}
              property={prop}
              onOpen={handleOpen}
              onFavoriteToggle={handleRemoveFavorite}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default Favorites;