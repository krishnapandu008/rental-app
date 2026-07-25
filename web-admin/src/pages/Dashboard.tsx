import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { getProperties } from '../api/propertyApi';
import { Property } from '../types';
import PropertyCard from '../components/PropertyCard/PropertyCard';
import styles from './Dashboard.module.scss';

const Dashboard: React.FC = () => {
  const { owner } = useAuth();
  const navigate = useNavigate();
  const [properties, setProperties] = useState<Property[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchLocation, setSearchLocation] = useState('');

  useEffect(() => {
    loadProperties();
  }, []);

  const loadProperties = async (location?: string) => {
    try {
      setLoading(true);
      const res = await getProperties(location ? { location } : undefined);
      setProperties(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    loadProperties(searchLocation.trim() || undefined);
  };

  const handleReset = () => {
    setSearchLocation('');
    loadProperties();
  };

  const handleOpen = (property: Property) => {
    navigate(`/property/${property.id}`, { state: { property } });
  };

  if (loading) return <div className={styles.loading}>Loading properties...</div>;

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2 className={styles.title}>Available Rentals</h2>
        {owner && (
          <button className={styles.addButton} onClick={() => navigate('/add')}>
            + Add Property
          </button>
        )}
      </div>

      {/* Search Bar */}
      <form className={styles.searchForm} onSubmit={handleSearch}>
        <input
          type="text"
          placeholder="Search by location..."
          value={searchLocation}
          onChange={(e) => setSearchLocation(e.target.value)}
          className={styles.searchInput}
        />
        <button type="submit" className={styles.searchBtn}>Search</button>
        {searchLocation && (
          <button type="button" className={styles.resetBtn} onClick={handleReset}>
            Reset
          </button>
        )}
      </form>

      {properties.length === 0 ? (
        <div className={styles.emptyState}>
          <p>No properties found matching your criteria.</p>
          {owner && <a href="/add">Add your first property →</a>}
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