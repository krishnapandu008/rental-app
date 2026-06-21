import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { addProperty } from '../api/propertyApi';

const AddProperty: React.FC = () => {
  const { owner } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    title: '',
    description: '',
    location: '',
    rent: 0,
    bedrooms: 1,
    contactNumber: '',
  });
  const [error, setError] = useState('');

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!owner) {
      setError('You must be logged in');
      return;
    }
    try {
      await addProperty({
        ...form,
        rent: Number(form.rent),
        bedrooms: Number(form.bedrooms),
        ownerId: owner.id,
      });
      navigate('/');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to add property');
    }
  };

  return (
    <div style={{ maxWidth: 600, margin: '2rem auto', padding: '1rem' }}>
      <h2>Add New Property</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: '1rem' }}>
          <label>Title</label>
          <input name="title" value={form.title} onChange={handleChange} required style={{ width: '100%', padding: '0.5rem' }} />
        </div>
        <div style={{ marginBottom: '1rem' }}>
          <label>Description</label>
          <textarea name="description" value={form.description} onChange={handleChange} style={{ width: '100%', padding: '0.5rem' }} />
        </div>
        <div style={{ marginBottom: '1rem' }}>
          <label>Location</label>
          <input name="location" value={form.location} onChange={handleChange} required style={{ width: '100%', padding: '0.5rem' }} />
        </div>
        <div style={{ marginBottom: '1rem' }}>
          <label>Rent (₹)</label>
          <input type="number" name="rent" value={form.rent} onChange={handleChange} required style={{ width: '100%', padding: '0.5rem' }} />
        </div>
        <div style={{ marginBottom: '1rem' }}>
          <label>Bedrooms</label>
          <input type="number" name="bedrooms" value={form.bedrooms} onChange={handleChange} required style={{ width: '100%', padding: '0.5rem' }} />
        </div>
        <div style={{ marginBottom: '1rem' }}>
          <label>Contact Number</label>
          <input name="contactNumber" value={form.contactNumber} onChange={handleChange} required style={{ width: '100%', padding: '0.5rem' }} />
        </div>
        <button type="submit" style={{ padding: '0.5rem 1rem', background: '#f4511e', color: 'white', border: 'none', borderRadius: '4px' }}>Add Property</button>
      </form>
    </div>
  );
};

export default AddProperty;