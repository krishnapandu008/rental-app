import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { getProperties, getMyProperties } from '../api/propertyApi';
import { Property } from '../types';
import PropertyCard from '../components/PropertyCard/PropertyCard';
import styles from './Dashboard.module.scss';

import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

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
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(10);
  const [showMyListings, setShowMyListings] = useState(false); // ✅ NEW

  const [searchLocation, setSearchLocation] = useState('');
  const [minPrice, setMinPrice] = useState<number | undefined>(undefined);
  const [maxPrice, setMaxPrice] = useState<number | undefined>(undefined);
  const [bedrooms, setBedrooms] = useState<number | undefined>(undefined);
  const [sortBy, setSortBy] = useState('newest');

  useEffect(() => {
    loadProperties();
  }, [currentPage, sortBy, showMyListings]);

  const loadProperties = async () => {
    try {
      setLoading(true);
      let res;
      if (showMyListings && owner) {
        res = await getMyProperties(owner.id);
        // For my listings, we treat the result as a plain array (not paginated)
        setProperties(res.data || []);
        setTotalPages(1);
      } else {
        res = await getProperties({
          location: searchLocation || undefined,
          minPrice,
          maxPrice,
          bedrooms,
          sortBy,
          page: currentPage,
          size: pageSize,
        });
        setProperties(res.data?.content || []);
        setTotalPages(res.data?.totalPages || 0);
      }
    } catch (err) {
      console.error(err);
      setProperties([]);
      setTotalPages(0);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setCurrentPage(0);
    loadProperties();
  };

  const handleReset = () => {
    setSearchLocation('');
    setMinPrice(undefined);
    setMaxPrice(undefined);
    setBedrooms(undefined);
    setSortBy('newest');
    setCurrentPage(0);
    if (showMyListings) setShowMyListings(false);
    loadProperties();
  };

  const handlePageChange = (newPage: number) => {
    if (newPage >= 0 && newPage < totalPages) {
      setCurrentPage(newPage);
    }
  };

  const handleOpen = (property: Property) => {
    navigate(`/property/${property.id}`, { state: { property } });
  };

  const toggleMyListings = () => {
    setShowMyListings(!showMyListings);
    setCurrentPage(0);
  };

  if (loading) return <div className={styles.loading}>Loading properties...</div>;

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2 className={styles.title}>Available Rentals</h2>
        <div className={styles.headerActions}>
          {owner && (
            <button className={styles.toggleBtn} onClick={toggleMyListings}>
              {showMyListings ? 'Show All Properties' : 'Show My Listings'}
            </button>
          )}
          {owner && (
            <button className={styles.addButton} onClick={() => navigate('/add')}>
              + Add Property
            </button>
          )}
        </div>
      </div>

      {!showMyListings && (
        <form className={styles.searchForm} onSubmit={handleSearch}>
          <input
            type="text"
            placeholder="Search by location..."
            value={searchLocation}
            onChange={(e) => setSearchLocation(e.target.value)}
            className={styles.searchInput}
          />
          <input
            type="number"
            placeholder="Min Price"
            value={minPrice ?? ''}
            onChange={(e) => setMinPrice(e.target.value ? Number(e.target.value) : undefined)}
            className={styles.filterInput}
          />
          <input
            type="number"
            placeholder="Max Price"
            value={maxPrice ?? ''}
            onChange={(e) => setMaxPrice(e.target.value ? Number(e.target.value) : undefined)}
            className={styles.filterInput}
          />
          <select
            value={bedrooms ?? ''}
            onChange={(e) => setBedrooms(e.target.value ? Number(e.target.value) : undefined)}
            className={styles.filterSelect}
          >
            <option value="">All Bedrooms</option>
            <option value="1">1 BHK</option>
            <option value="2">2 BHK</option>
            <option value="3">3 BHK</option>
            <option value="4">4+ BHK</option>
          </select>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className={styles.filterSelect}
          >
            <option value="newest">Newest First</option>
            <option value="price_asc">Price: Low to High</option>
            <option value="price_desc">Price: High to Low</option>
          </select>
          <button type="submit" className={styles.searchBtn}>Search</button>
          <button type="button" className={styles.resetBtn} onClick={handleReset}>
            Reset
          </button>
        </form>
      )}

      <div className={styles.mapContainer}>
        <MapContainer
          center={[20.5937, 78.9629]}
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
        <>
          <div className={styles.propertyGrid}>
            {properties.map((prop) => (
              <PropertyCard
                key={prop.id}
                property={prop}
                onOpen={handleOpen}
              />
            ))}
          </div>

          {!showMyListings && totalPages > 1 && (
            <div className={styles.pagination}>
              <button
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 0}
                className={styles.pageBtn}
              >
                Previous
              </button>
              <span className={styles.pageInfo}>
                Page {currentPage + 1} of {totalPages}
              </span>
              <button
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage === totalPages - 1}
                className={styles.pageBtn}
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default Dashboard;