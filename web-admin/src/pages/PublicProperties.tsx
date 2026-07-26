import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getProperties } from '../api/propertyApi';
import { Property } from '../types';
import PropertyCard from '../components/PropertyCard/PropertyCard';
import styles from './PublicProperties.module.scss';

const PublicProperties: React.FC = () => {
  const navigate = useNavigate();
  const [properties, setProperties] = useState<Property[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadProperties();
  }, []);

  const loadProperties = async () => {
    try {
      const res = await getProperties(); // Now returns PageResponse<Property>
      // ✅ Access the content array from the paginated response
      setProperties(res.data.content || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpen = (property: Property) => {
    navigate(`/property/${property.id}`, { state: { property } });
  };

  if (loading) return <div className={styles.loading}>Loading properties...</div>;

  return (
    <div className={styles.container}>
      <h2 className={styles.title}>Available Rentals</h2>
      {properties.length === 0 ? (
        <div className={styles.emptyState}>
          <p>No properties available right now.</p>
        </div>
      ) : (
        <div className={styles.propertyGrid}>
          {properties.map((prop) => (
            <PropertyCard key={prop.id} property={prop} onOpen={handleOpen} />
          ))}
        </div>
      )}
    </div>
  );
};

export default PublicProperties;