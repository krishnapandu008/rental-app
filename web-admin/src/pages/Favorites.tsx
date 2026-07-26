import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getFavoriteIds, getProperties } from '../api/propertyApi';
import { Property } from '../types';
import PropertyCard from '../components/PropertyCard/PropertyCard';
import styles from './Favorites.module.scss';

const Favorites: React.FC = () => {
  const navigate = useNavigate();
  const [favorites, setFavorites] = useState<Property[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadFavorites();
  }, []);

  const loadFavorites = async () => {
    try {
      setLoading(true);
      const idsRes = await getFavoriteIds();
      const ids = idsRes.data;
      if (ids.length === 0) {
        setFavorites([]);
        setLoading(false);
        return;
      }
      const propsRes = await getProperties({ size: 100 });
      const allProperties = propsRes.data.content || [];
      const favoriteProps = allProperties.filter(p => ids.includes(p.id));
      setFavorites(favoriteProps);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpen = (property: Property) => {
    navigate(`/property/${property.id}`, { state: { property } });
  };

  if (loading) return <div className={styles.loading}>Loading favorites...</div>;

  return (
    <div className={styles.container}>
      <h2 className={styles.title}>❤️ My Favorites</h2>
      {favorites.length === 0 ? (
        <div className={styles.emptyState}>
          <p>You haven't saved any properties yet.</p>
          <a href="/">Browse properties →</a>
        </div>
      ) : (
        <div className={styles.propertyGrid}>
          {favorites.map((prop) => (
            <PropertyCard key={prop.id} property={prop} onOpen={handleOpen} />
          ))}
        </div>
      )}
    </div>
  );
};

export default Favorites;