import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { api } from '../api/client';
import styles from './EditProperty.module.scss';

// ===== ICON COMPONENTS =====
const BackIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="15 18 9 12 15 6" />
  </svg>
);

const DeleteIcon = () => (
  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="3 6 5 6 21 6" />
    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
    <line x1="10" y1="11" x2="10" y2="17" />
    <line x1="14" y1="11" x2="14" y2="17" />
  </svg>
);

const ReplaceIcon = () => (
  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="23 4 23 10 17 10" />
    <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
  </svg>
);

const EditProperty: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { owner } = useAuth();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const [form, setForm] = useState({
    title: '',
    description: '',
    location: '',
    rent: 0,
    bedrooms: 1,
    contactNumber: '',
    available: true,
    visibility: 'PUBLIC' as 'PUBLIC' | 'PRIVATE' | 'UNLISTED',
    latitude: 0,   // ✅ NEW
    longitude: 0,  // ✅ NEW
  });

  const [existingImages, setExistingImages] = useState<string[]>([]);
  const [newFiles, setNewFiles] = useState<File[]>([]);
  const [previews, setPreviews] = useState<string[]>([]);

  // Load property details
  useEffect(() => {
    if (!owner || !id) return;
    api
      .get(`/properties/${id}`)
      .then((res) => {
        const data = res.data;
        setForm({
          title: data.title || '',
          description: data.description || '',
          location: data.location || '',
          rent: data.rent || 0,
          bedrooms: data.bedrooms || 1,
          contactNumber: data.contactNumber || '',
          available: data.available ?? true,
          visibility: data.visibility || 'PUBLIC',
          latitude: data.latitude || 0,   // ✅ NEW
          longitude: data.longitude || 0, // ✅ NEW
        });
        setExistingImages(data.imageUrls || []);
        setLoading(false);
      })
      .catch((err) => {
        console.error(err);
        setError('Failed to load property details');
        setLoading(false);
      });
  }, [id, owner]);

  // Form field changes (supports input, textarea, and select)
  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    const { name, value, type } = e.target;
    const checked = (e.target as HTMLInputElement).checked;
    setForm((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  // New image selection
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const selected = Array.from(e.target.files);
      setNewFiles(selected);
      const urls = selected.map((file) => URL.createObjectURL(file));
      setPreviews(urls);
    }
  };

  // Delete an existing image
  const deleteExistingImage = async (url: string) => {
    if (!window.confirm('Remove this image from the property?')) return;
    try {
      await api.delete('/properties/images', { data: { url } });
      setExistingImages((prev) => prev.filter((img) => img !== url));
    } catch (err) {
      alert('Failed to delete image');
    }
  };

  // Replace an existing image
  const replaceExistingImage = (oldUrl: string) => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.onchange = async (e: any) => {
      const file = e.target.files?.[0];
      if (file) {
        try {
          // First delete the old image
          await api.delete('/properties/images', { data: { url: oldUrl } });
          // Then upload the new one
          const formData = new FormData();
          formData.append('images', file);
          const res = await api.post(`/properties/${id}/images`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
          });
          // Update the URL in the existing images list
          const newUrl = res.data?.[0] || URL.createObjectURL(file);
          setExistingImages((prev) =>
            prev.map((url) => (url === oldUrl ? newUrl : url))
          );
          alert('Image replaced successfully!');
        } catch (err) {
          alert('Failed to replace image');
        }
      }
    };
    input.click();
  };

  // Submit form
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!owner) return;
    try {
      setSaving(true);
      setError('');

      // 1. Update property details
      await api.put(`/properties/${id}`, form);

      // 2. Upload new images (if any)
      if (newFiles.length > 0) {
        const formData = new FormData();
        newFiles.forEach((file) => formData.append('images', file));
        await api.post(`/properties/${id}/images`, formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });
      }

      // Navigate back to Property Details page
      navigate(`/property/${id}`);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update property');
    } finally {
      setSaving(false);
    }
  };

  // Go back to Property Details
  const handleCancel = () => {
    navigate(`/property/${id}`);
  };

  if (loading)
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>Loading...</div>
    );

  return (
    <div className={styles.editContainer}>
      {/* ===== HEADER WITH BACK BUTTON ===== */}
      <div className={styles.header}>
        <button
          className={styles.backButton}
          onClick={handleCancel}
          title="Back to Property"
        >
          <BackIcon />
        </button>
        <h2>Edit Property</h2>
      </div>

      {error && <div className={styles.error}>{error}</div>}

      <form className={styles.form} onSubmit={handleSubmit}>
        {/* Title */}
        <div className={styles.formGroup}>
          <label>Title</label>
          <input
            name="title"
            value={form.title}
            onChange={handleChange}
            required
          />
        </div>

        {/* Description */}
        <div className={styles.formGroup}>
          <label>Description</label>
          <textarea
            name="description"
            value={form.description}
            onChange={handleChange}
          />
        </div>

        {/* Location */}
        <div className={styles.formGroup}>
          <label>Location</label>
          <input
            name="location"
            value={form.location}
            onChange={handleChange}
            required
          />
        </div>

        {/* Rent */}
        <div className={styles.formGroup}>
          <label>Rent (₹)</label>
          <input
            type="number"
            name="rent"
            value={form.rent}
            onChange={handleChange}
            required
          />
        </div>

        {/* Bedrooms */}
        <div className={styles.formGroup}>
          <label>Bedrooms</label>
          <input
            type="number"
            name="bedrooms"
            value={form.bedrooms}
            onChange={handleChange}
            required
          />
        </div>

        {/* Contact Number */}
        <div className={styles.formGroup}>
          <label>Contact Number</label>
          <input
            name="contactNumber"
            value={form.contactNumber}
            onChange={handleChange}
            required
          />
        </div>

        {/* Availability */}
        <div className={styles.formGroup}>
          <label className={styles.checkboxLabel}>
            <input
              type="checkbox"
              name="available"
              checked={form.available}
              onChange={handleChange}
            />
            Available
          </label>
        </div>

        {/* ===== Visibility Dropdown ===== */}
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

        {/* ===== IMAGE SECTION ===== */}
        <div className={styles.imageSection}>
          {existingImages.length > 0 && (
            <>
              <span className={styles.sectionLabel}>Existing Images</span>
              <div className={styles.imageGrid}>
                {existingImages.map((url, idx) => (
                  <div key={idx} className={styles.thumbnailWrapper}>
                    <img
                      src={url}
                      alt={`Property image ${idx + 1}`}
                      className={styles.thumbnail}
                    />
                    <div className={styles.imageControls}>
                      {/* Replace Button */}
                      <button
                        type="button"
                        className={`${styles.controlBtn} ${styles.replaceBtn}`}
                        onClick={(e) => {
                          e.stopPropagation();
                          replaceExistingImage(url);
                        }}
                        title="Replace this image"
                      >
                        <ReplaceIcon />
                      </button>
                      {/* Delete Button */}
                      <button
                        type="button"
                        className={`${styles.controlBtn} ${styles.deleteBtn}`}
                        onClick={(e) => {
                          e.stopPropagation();
                          deleteExistingImage(url);
                        }}
                        title="Delete this image"
                      >
                        <DeleteIcon />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>

        {/* Upload new images */}
        <div className={styles.imageSection}>
          <span className={styles.sectionLabel}>Add New Images</span>
          <div className={styles.fileInputWrapper}>
            <input
              type="file"
              multiple
              accept="image/*"
              onChange={handleFileChange}
            />
          </div>
          {previews.length > 0 && (
            <div className={styles.imageGrid} style={{ marginTop: '0.5rem' }}>
              {previews.map((url, idx) => (
                <div key={idx} className={styles.thumbnailWrapper}>
                  <img
                    src={url}
                    alt={`Preview ${idx + 1}`}
                    className={styles.thumbnail}
                  />
                </div>
              ))}
            </div>
          )}
        </div>

        {/* ===== FORM ACTIONS ===== */}
        <div className={styles.formActions}>
          <button
            type="submit"
            disabled={saving}
            className={styles.submitBtn}
          >
            {saving ? 'Saving...' : '💾 Update Property'}
          </button>
          <button
            type="button"
            onClick={handleCancel}
            className={styles.cancelBtn}
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
};

export default EditProperty;