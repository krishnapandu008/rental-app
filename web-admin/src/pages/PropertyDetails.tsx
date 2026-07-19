import React, { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { Property } from '../types';
import { formatCurrency } from '../utils/helpers';
import { deleteProperty, togglePropertyActive } from '../api/propertyApi';
import { api } from '../api/client';
import { useAuth } from '../contexts/AuthContext';
import styles from './PropertyDetails.module.scss';

// ===== ICON COMPONENTS =====
const BackIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="15 18 9 12 15 6" />
  </svg>
);

const DeleteIcon = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="3 6 5 6 21 6" />
    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
    <line x1="10" y1="11" x2="10" y2="17" />
    <line x1="14" y1="11" x2="14" y2="17" />
  </svg>
);

const ReplaceIcon = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="23 4 23 10 17 10" />
    <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
  </svg>
);

const CloseIcon = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <line x1="18" y1="6" x2="6" y2="18" />
    <line x1="6" y1="6" x2="18" y2="18" />
  </svg>
);

// ===== CUSTOM LIGHTBOX COMPONENT =====
interface LightboxProps {
  images: string[];
  initialIndex: number;
  onClose: () => void;
}

const ImageLightbox: React.FC<LightboxProps> = ({ images, initialIndex, onClose }) => {
  const [currentIndex, setCurrentIndex] = useState(initialIndex);
  const [scale, setScale] = useState(1);
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 });
  const [startPos, setStartPos] = useState({ x: 0, y: 0 });
  const imgRef = useRef<HTMLImageElement>(null);

  const currentImage = images[currentIndex];

  const handlePrev = () => {
    setCurrentIndex((prev) => (prev > 0 ? prev - 1 : images.length - 1));
    setScale(1);
    setPosition({ x: 0, y: 0 });
  };

  const handleNext = () => {
    setCurrentIndex((prev) => (prev < images.length - 1 ? prev + 1 : 0));
    setScale(1);
    setPosition({ x: 0, y: 0 });
  };

  const handleZoomToggle = () => {
    if (scale === 1) {
      setScale(2.5);
    } else {
      setScale(1);
      setPosition({ x: 0, y: 0 });
    }
  };

  const handleMouseDown = (e: React.MouseEvent) => {
    if (scale === 1) return;
    setIsDragging(true);
    setDragStart({ x: e.clientX, y: e.clientY });
    setStartPos({ x: position.x, y: position.y });
  };

  const handleMouseMove = (e: React.MouseEvent) => {
    if (!isDragging) return;
    const dx = e.clientX - dragStart.x;
    const dy = e.clientY - dragStart.y;
    setPosition({
      x: startPos.x + dx,
      y: startPos.y + dy,
    });
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  const handleWheel = (e: React.WheelEvent) => {
    e.preventDefault();
    const delta = e.deltaY > 0 ? -0.2 : 0.2;
    setScale((prev) => Math.min(Math.max(prev + delta, 1), 4));
    if (scale + delta < 1.2) {
      setPosition({ x: 0, y: 0 });
    }
  };

  // Keyboard shortcuts
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
      if (e.key === 'ArrowLeft') handlePrev();
      if (e.key === 'ArrowRight') handleNext();
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onClose, handlePrev, handleNext]);

  // Prevent body scroll
  useEffect(() => {
    document.body.style.overflow = 'hidden';
    return () => { document.body.style.overflow = 'unset'; };
  }, []);

  return (
    <div className={styles.lightboxOverlay} onClick={onClose}>
      {/* Toolbar */}
      <div className={styles.lightboxToolbar}>
        <span className={styles.lightboxCounter}>
          {currentIndex + 1} / {images.length}
        </span>
        <button className={styles.lightboxClose} onClick={onClose}>
          <CloseIcon />
        </button>
      </div>

      {/* Image Container */}
      <div
        className={styles.lightboxImageContainer}
        onClick={(e) => e.stopPropagation()}
        onMouseDown={handleMouseDown}
        onMouseMove={handleMouseMove}
        onMouseUp={handleMouseUp}
        onMouseLeave={handleMouseUp}
        onWheel={handleWheel}
        style={{ cursor: scale > 1 ? (isDragging ? 'grabbing' : 'grab') : 'zoom-in' }}
      >
        <img
          ref={imgRef}
          src={currentImage}
          alt={`Property ${currentIndex + 1}`}
          className={styles.lightboxImage}
          style={{
            transform: `scale(${scale}) translate(${position.x / scale}px, ${position.y / scale}px)`,
            transition: isDragging ? 'none' : 'transform 0.2s ease',
          }}
          onClick={(e) => {
            e.stopPropagation();
            handleZoomToggle();
          }}
          draggable={false}
        />
      </div>

      {/* Navigation Arrows */}
      {images.length > 1 && (
        <>
          <button
            className={`${styles.lightboxNav} ${styles.lightboxNavPrev}`}
            onClick={(e) => { e.stopPropagation(); handlePrev(); }}
          >
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="15 18 9 12 15 6" />
            </svg>
          </button>
          <button
            className={`${styles.lightboxNav} ${styles.lightboxNavNext}`}
            onClick={(e) => { e.stopPropagation(); handleNext(); }}
          >
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="9 18 15 12 9 6" />
            </svg>
          </button>
        </>
      )}
    </div>
  );
};

// ===== MAIN COMPONENT =====
const PropertyDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const location = useLocation();
  const navigate = useNavigate();
  const { owner } = useAuth();

  const [property, setProperty] = useState<Property | null>(
    location.state?.property || null
  );
  const [loading, setLoading] = useState(!property);
  const [error, setError] = useState('');
  const [mainImage, setMainImage] = useState<string>('');
  const [lightboxOpen, setLightboxOpen] = useState(false);
  const [lightboxIndex, setLightboxIndex] = useState(0);
  const [togglingActive, setTogglingActive] = useState(false);

  const isAdmin = owner?.role === 'ADMIN';

  // Fetch property if not passed via state
  useEffect(() => {
    if (property) {
      setMainImage(property.imageUrls?.[0] || '');
      return;
    }

    if (!id) {
      setError('Property ID not found');
      setLoading(false);
      return;
    }

    const fetchProperty = async () => {
      try {
        const res = await api.get(`/properties/${id}`);
        const data = res.data;
        setProperty(data);
        setMainImage(data.imageUrls?.[0] || '');
      } catch (err) {
        setError('Failed to load property details');
      } finally {
        setLoading(false);
      }
    };

    fetchProperty();
  }, [id, property]);

  const handleDelete = async () => {
    if (!property) return;
    if (window.confirm('Are you sure you want to permanently delete this property? This cannot be undone.')) {
      try {
        await deleteProperty(property.id);
        navigate('/');
      } catch (err) {
        alert('Failed to delete property. Please try again.');
      }
    }
  };

  const handleDeleteImage = async (imageUrl: string) => {
    if (!property) return;
    if (window.confirm('Remove this image from the property?')) {
      try {
        await api.delete('/properties/images', { data: { url: imageUrl } });
        setProperty((prev) => prev ? {
          ...prev,
          imageUrls: prev.imageUrls.filter((url) => url !== imageUrl)
        } : null);
        if (mainImage === imageUrl) {
          setMainImage(property.imageUrls[0] || '');
        }
      } catch (err) {
        alert('Failed to delete image.');
      }
    }
  };

  const handleReplaceImage = (oldImageUrl: string) => {
    if (!property) return;
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.onchange = async (e: any) => {
      const file = e.target.files?.[0];
      if (file) {
        try {
          await api.delete('/properties/images', { data: { url: oldImageUrl } });
          const formData = new FormData();
          formData.append('images', file);
          const res = await api.post(`/properties/${id}/images`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
          });
          const newUrl = res.data?.[0] || URL.createObjectURL(file);
          setProperty((prev) => prev ? {
            ...prev,
            imageUrls: prev.imageUrls.map((url) => url === oldImageUrl ? newUrl : url)
          } : null);
          if (mainImage === oldImageUrl) {
            setMainImage(newUrl);
          }
        } catch (err) {
          alert('Failed to replace image.');
        }
      }
    };
    input.click();
  };

  const openLightbox = (index: number) => {
    setLightboxIndex(index);
    setLightboxOpen(true);
  };

  // Admin-only: toggle active status
  const handleToggleActive = async () => {
    if (!property || !isAdmin) return;
    try {
      setTogglingActive(true);
      await togglePropertyActive(property.id, !property.isActive);
      setProperty((prev) => prev ? { ...prev, isActive: !prev.isActive } : null);
      alert(`Property ${!property.isActive ? 'activated' : 'deactivated'} successfully.`);
    } catch (err) {
      alert('Failed to change active status.');
    } finally {
      setTogglingActive(false);
    }
  };

  if (loading) {
    return <div style={{ padding: '2rem', textAlign: 'center' }}>Loading property details...</div>;
  }

  if (error || !property) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <p>{error || 'Property not found'}</p>
        <button onClick={() => navigate('/')}>Go to Dashboard</button>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      {/* ===== BACK BUTTON ===== */}
      <button className={styles.backButton} onClick={() => navigate('/')}>
        <BackIcon /> Back
      </button>

      <div className={styles.card}>
        {/* ===== HEADER ===== */}
        <div className={styles.header}>
          <h1 className={styles.title}>{property.title}</h1>

          <div className={styles.actionButtons}>
            {/* Edit Button */}
            <button
              className={styles.actionBtn}
              onClick={() => navigate(`/edit/${property.id}`, { state: { property } })}
              title="Edit Property"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
              </svg>
            </button>

            {/* Delete Property Button */}
            <button
              className={`${styles.actionBtn} ${styles.deleteActionBtn}`}
              onClick={handleDelete}
              title="Delete Property"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="3 6 5 6 21 6" />
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                <line x1="10" y1="11" x2="10" y2="17" />
                <line x1="14" y1="11" x2="14" y2="17" />
              </svg>
            </button>
          </div>
        </div>

        {/* ===== IMAGE GALLERY ===== */}
        <div className={styles.imageGallery}>
          {property.imageUrls && property.imageUrls.length > 0 ? (
            <>
              {/* Main Image */}
              <img
                src={mainImage}
                alt={property.title}
                className={styles.mainImage}
                onClick={() => openLightbox(0)}
                style={{ cursor: 'zoom-in' }}
              />

              {/* Thumbnails */}
              {property.imageUrls.length > 1 && (
                <div className={styles.thumbnailGrid}>
                  {property.imageUrls.map((url, idx) => {
                    const isMain = mainImage === url;
                    return (
                      <div
                        key={idx}
                        className={`${styles.thumbnailWrapper} ${isMain ? styles.active : ''}`}
                      >
                        <img
                          src={url}
                          alt={`${property.title} ${idx + 1}`}
                          className={`${styles.thumbnailImg} ${isMain ? styles.active : ''}`}
                          onClick={() => {
                            setMainImage(url);
                            setLightboxIndex(idx);
                          }}
                          title={isMain ? 'Current main image' : 'Click to make main'}
                          style={{ cursor: 'pointer' }}
                        />

                        {/* Image Controls */}
                        <div className={styles.imageControls}>
                          <button
                            className={`${styles.controlBtn} ${styles.replaceBtn}`}
                            onClick={(e) => {
                              e.stopPropagation();
                              handleReplaceImage(url);
                            }}
                            title="Replace this image"
                          >
                            <ReplaceIcon />
                          </button>
                          <button
                            className={`${styles.controlBtn} ${styles.deleteBtn}`}
                            onClick={(e) => {
                              e.stopPropagation();
                              handleDeleteImage(url);
                            }}
                            title="Delete this image"
                          >
                            <DeleteIcon />
                          </button>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </>
          ) : (
            <div className={styles.noImagePlaceholder}>
              No Images Available
            </div>
          )}
        </div>

        {/* ===== PROPERTY DETAILS ===== */}
        <div className={styles.detailsGrid}>
          <p><strong>📍 Location:</strong> {property.location}</p>
          <p><strong>💰 Rent:</strong> {formatCurrency(property.rent)} / month</p>
          <p><strong>🛏️ Bedrooms:</strong> {property.bedrooms} BHK</p>
          <p><strong>📞 Contact:</strong> {property.contactNumber}</p>
          {property.description && (
            <p><strong>📝 Description:</strong> {property.description}</p>
          )}
          {/* NEW FIELDS */}
          <p><strong>👁️ Visibility:</strong> {property.visibility}</p>
          <p>
            <strong>📌 Status:</strong> 
            <span className={property.isActive ? styles.activeBadge : styles.inactiveBadge}>
              {property.isActive ? 'Active' : 'Inactive (hidden)'}
            </span>
          </p>
        </div>

        {/* Admin-only toggle for active status */}
        {isAdmin && (
          <div className={styles.adminActions}>
            <button
              onClick={handleToggleActive}
              disabled={togglingActive}
              className={styles.toggleActiveBtn}
            >
              {togglingActive ? 'Processing...' : property.isActive ? 'Deactivate' : 'Activate'}
            </button>
            <span className={styles.adminHint}>Admin: toggle visibility for all users</span>
          </div>
        )}
      </div>

      {/* ===== LIGHTBOX ===== */}
      {lightboxOpen && (
        <ImageLightbox
          images={property.imageUrls}
          initialIndex={lightboxIndex}
          onClose={() => setLightboxOpen(false)}
        />
      )}
    </div>
  );
};

export default PropertyDetails;