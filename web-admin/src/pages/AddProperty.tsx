import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { addProperty } from '../api/propertyApi';
import styles from './AddProperty.module.scss';
import AmenitiesCheckboxes from '../components/AmenitiesCheckboxes/AmenitiesCheckboxes';

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
    visibility: 'PUBLIC' as 'PUBLIC' | 'PRIVATE' | 'UNLISTED',
    latitude: 0,   // ✅ NEW
    longitude: 0,  // ✅ NEW
    amenities: [] as string[],
  });
  const [files, setFiles] = useState<File[]>([]);
  const [previews, setPreviews] = useState<string[]>([]);
  const [error, setError] = useState('');
  const [uploading, setUploading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const selected = Array.from(e.target.files);
      setFiles(selected);
      const urls = selected.map(file => URL.createObjectURL(file));
      setPreviews(urls);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!owner) {
      setError('You must be logged in');
      return;
    }
    try {
      setUploading(true);
      await addProperty(form, files);
      navigate('/');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to add property');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className={styles.container}>
      <h2 className={styles.title}>Add New Property</h2>
      {error && <div className={styles.error}>{error}</div>}

      <form className={styles.form} onSubmit={handleSubmit}>
        {/* ... existing fields ... (keep them) */}
        <div className={styles.formGroup}>
          <label>Title</label>
          <input name="title" value={form.title} onChange={handleChange} required />
        </div>
        <div className={styles.formGroup}>
          <label>Description</label>
          <textarea name="description" value={form.description} onChange={handleChange} />
        </div>
        <div className={styles.formGroup}>
          <label>Location</label>
          <input name="location" value={form.location} onChange={handleChange} required />
        </div>
        <div className={styles.formGroup}>
          <label>Rent (₹)</label>
          <input type="number" name="rent" value={form.rent} onChange={handleChange} required />
        </div>
        <div className={styles.formGroup}>
          <label>Bedrooms</label>
          <input type="number" name="bedrooms" value={form.bedrooms} onChange={handleChange} required />
        </div>
        <div className={styles.formGroup}>
          <label>Contact Number</label>
          <input name="contactNumber" value={form.contactNumber} onChange={handleChange} required />
        </div>
        <div className={styles.formGroup}>
          <label>Visibility</label>
          <select name="visibility" value={form.visibility} onChange={handleChange}>
            <option value="PUBLIC">Public (anyone can view)</option>
            <option value="PRIVATE">Private (only you and admins)</option>
            <option value="UNLISTED">Unlisted (only admins)</option>
          </select>
        </div>

        {/* ✅ NEW: Latitude & Longitude inputs */}
        <div className={styles.formGroup}>
          <label>Latitude</label>
          <input
            type="number"
            step="any"
            name="latitude"
            value={form.latitude || ''}
            onChange={handleChange}
            placeholder="e.g., 12.9716"
          />
        </div>
        <div className={styles.formGroup}>
          <label>Longitude</label>
          <input
            type="number"
            step="any"
            name="longitude"
            value={form.longitude || ''}
            onChange={handleChange}
            placeholder="e.g., 77.5946"
          />
        </div>

        <div className={styles.formGroup}>
          <label>Amenities</label>
          <AmenitiesCheckboxes value={form.amenities} onChange={(next) => setForm(prev => ({ ...prev, amenities: next }))} />
        </div>

        <div className={styles.formGroup}>
          <label>Images (select multiple)</label>
          <div className={styles.fileInputWrapper}>
            <input type="file" multiple accept="image/*" onChange={handleFileChange} />
          </div>
          {previews.length > 0 && (
            <div className={styles.previewGrid}>
              {previews.map((url, idx) => (
                <img key={idx} src={url} alt={`preview-${idx}`} className={styles.previewImage} />
              ))}
            </div>
          )}
        </div>

        <button type="submit" disabled={uploading} className={styles.submitBtn}>
          {uploading ? 'Uploading...' : 'Add Property'}
        </button>
      </form>
    </div>
  );
};

export default AddProperty;