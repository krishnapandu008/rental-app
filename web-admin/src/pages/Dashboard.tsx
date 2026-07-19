import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { getProperties } from '../api/propertyApi';  // ✅ changed import
import { Property } from '../types';
import PropertyCard from '../components/PropertyCard/PropertyCard';
import styles from './Dashboard.module.scss';

const Dashboard: React.FC = () => {
  const { owner } = useAuth();
  const navigate = useNavigate();
  const [properties, setProperties] = useState<Property[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (owner) {
      loadProperties();
    } else {
      setLoading(false);
    }
  }, [owner]);

  const loadProperties = async () => {
    try {
      // ✅ Fetch ALL visible properties (PUBLIC + owner's own PRIVATE/UNLISTED)
      const res = await getProperties();
      setProperties(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpen = (property: Property) => {
    navigate(`/property/${property.id}`, { state: { property } });
  };

  if (loading) return <div className={styles.loading}>Loading...</div>;
  if (!owner) return <div className={styles.loading}>Please login</div>;

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2 className={styles.title}>Properties</h2>  {/* ✅ updated title */}
        <button className={styles.addButton} onClick={() => navigate('/add')}>
          + Add Property
        </button>
      </div>

      {properties.length === 0 ? (
        <div className={styles.emptyState}>
          <p>No properties available.</p>  {/* ✅ updated empty message */}
          <a href="/add">Add your first property →</a>
        </div>
      ) : (
        <div className={styles.propertyGrid}>
          {properties.map((prop) => (
            <PropertyCard
              key={prop.id}
              property={prop}
              onOpen={handleOpen}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default Dashboard;