import React, { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { getMyProperties, deleteProperty } from '../api/propertyApi';
import { Property } from '../types';
import PropertyCard from '../components/PropertyCard/PropertyCard';

const Dashboard: React.FC = () => {
  const { owner } = useAuth();
  const [properties, setProperties] = useState<Property[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (owner) {
      loadProperties();
    }
  }, [owner]);

  const loadProperties = async () => {
    try {
      const res = await getMyProperties(owner!.id);
      setProperties(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    await deleteProperty(id);
    setProperties(prev => prev.filter(p => p.id !== id));
  };

  if (loading) return <div>Loading...</div>;
  if (!owner) return <div>Please login</div>;

  return (
    <div style={{ padding: '2rem' }}>
      <h2>My Properties</h2>
      {properties.length === 0 ? (
        <p>No properties added yet. <a href="/add">Add one</a></p>
      ) : (
        <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))' }}>
          {properties.map(prop => (
            <PropertyCard key={prop.id} property={prop} onDelete={handleDelete} />
          ))}
        </div>
      )}
    </div>
  );
};

export default Dashboard;