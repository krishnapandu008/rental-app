import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { getProperties } from '../api/propertyApi';
import { Property } from '../types';
import PropertyCard from '../components/PropertyCard/PropertyCard';
import styles from './Dashboard.module.scss';

// ✅ Leaflet imports
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

// ✅ Fix marker icons using CDN URLs (no require)
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

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

      {/* ✅ Map */}
      <div className={styles.mapContainer}>
        <MapContainer
          center={[20.5937, 78.9629]} // Center of India
          zoom={5}
          style={{ height: '400px', width: '100%' }}
        >
          <TileLayer
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          />
          {properties.filter(p => p.latitude && p.longitude).map((p) => (
            <Marker key={p.id} position={[p.latitude!, p.longitude!]}>
              <Popup>
                <strong>{p.title}</strong><br />
                {p.location}<br />
                ₹{p.rent}/month<br />
                <a href={`/property/${p.id}`}>View Details</a>
              </Popup>
            </Marker>
          ))}
        </MapContainer>
      </div>

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