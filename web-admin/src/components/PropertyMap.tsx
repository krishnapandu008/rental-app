import React, { useEffect, useState, useRef } from 'react';

interface Property {
  id: number;
  title: string;
  location: string;
  rent: number;
  bedrooms: number;
  latitude?: number;  // ✅ Make it optional
  longitude?: number; // ✅ Make it optional
  imageUrls: string[];
}

interface PropertyMapProps {
  properties: Property[];
  center: [number, number];
  zoom: number;
  onPropertyClick: (id: number) => void;
}

const PropertyMap: React.FC<PropertyMapProps> = ({
  properties,
  center,
  zoom,
  onPropertyClick,
}) => {
  const [mapReady, setMapReady] = useState(false);
  const mapInitialized = useRef(false);
  const mapRef = useRef<any>(null);
  const markersRef = useRef<any[]>([]);

  // ✅ Filter properties that have both latitude and longitude
  const propsWithCoords = properties.filter(
    (p): p is Property & { latitude: number; longitude: number } =>
      p.latitude !== undefined && p.longitude !== undefined && p.latitude !== null && p.longitude !== null
  );

  useEffect(() => {
    if (mapInitialized.current) {
      console.log('⏭️ Map already initialized, skipping...');
      return;
    }

    let isMounted = true;

    const loadMap = async () => {
      try {
        console.log('📦 Loading Leaflet dynamically...');
        
        const L = (await import('leaflet')).default;
        await import('leaflet/dist/leaflet.css');

        console.log('✅ Leaflet loaded successfully');

        delete (L.Icon.Default.prototype as any)._getIconUrl;
        L.Icon.Default.mergeOptions({
          iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
          iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
          shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
        });

        if (!isMounted) return;

        const container = document.getElementById('map-container');
        if (!container) {
          console.error('❌ Map container not found');
          return;
        }

        const map = L.map(container, {
          center: center,
          zoom: zoom,
          zoomControl: true,
          fadeAnimation: true,
        });

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        }).addTo(map);

        const createPriceMarker = (rent: number) => {
          const canvas = document.createElement('canvas');
          canvas.width = 41;
          canvas.height = 50;
          const ctx = canvas.getContext('2d')!;

          ctx.fillStyle = '#f4511e';
          ctx.strokeStyle = '#ffffff';
          ctx.lineWidth = 2;
          ctx.beginPath();
          ctx.moveTo(20.5, 49);
          ctx.lineTo(1, 20.5);
          ctx.quadraticCurveTo(1, 1, 20.5, 1);
          ctx.quadraticCurveTo(40, 1, 40, 20.5);
          ctx.lineTo(20.5, 49);
          ctx.closePath();
          ctx.fill();
          ctx.stroke();

          ctx.fillStyle = '#ffffff';
          ctx.font = 'bold 11px Arial';
          ctx.textAlign = 'center';
          ctx.textBaseline = 'middle';
          const priceText = `₹${Math.round(rent / 1000)}k`;
          ctx.fillText(priceText, 20.5, 28);

          const iconUrl = canvas.toDataURL('image/png');
          return L.icon({
            iconUrl: iconUrl,
            iconSize: [41, 50],
            iconAnchor: [20.5, 50],
            popupAnchor: [0, -45],
          });
        };

        const markers: any[] = [];
        propsWithCoords.forEach((p) => {
          const marker = L.marker([p.latitude, p.longitude], {
            icon: createPriceMarker(p.rent),
          })
            .addTo(map)
            .bindPopup(`
              <div style="padding: 4px; min-width: 180px;">
                <strong>${p.title}</strong><br />
                ${p.location}<br />
                <strong>₹${p.rent.toLocaleString()}</strong> / month<br />
                ${p.bedrooms} BHK<br />
                <button 
                  style="
                    margin-top: 8px;
                    padding: 4px 16px;
                    background: #f4511e;
                    color: white;
                    border: none;
                    border-radius: 4px;
                    cursor: pointer;
                    font-size: 12px;
                    font-weight: 600;
                  "
                  onclick="window.__mapPropertyClick(${p.id})"
                >
                  View Details
                </button>
              </div>
            `);
          markers.push(marker);
        });

        (window as any).__mapPropertyClick = (id: number) => {
          onPropertyClick(id);
        };

        mapRef.current = map;
        markersRef.current = markers;
        setMapReady(true);
        mapInitialized.current = true;

        if (propsWithCoords.length > 1) {
          const group = L.featureGroup(markers);
          map.fitBounds(group.getBounds(), { padding: [50, 50] });
        }

        console.log('✅ Map initialized with', propsWithCoords.length, 'markers');

        const handleResize = () => {
          setTimeout(() => {
            if (mapRef.current) {
              mapRef.current.invalidateSize();
            }
          }, 100);
        };
        window.addEventListener('resize', handleResize);

        return () => {
          window.removeEventListener('resize', handleResize);
        };
      } catch (error) {
        console.error('❌ Failed to load Leaflet:', error);
      }
    };

    loadMap();

    return () => {
      isMounted = false;
      if (mapRef.current) {
        mapRef.current.remove();
        mapRef.current = null;
      }
      if (markersRef.current.length > 0) {
        markersRef.current.forEach(m => m.remove());
        markersRef.current = [];
      }
      delete (window as any).__mapPropertyClick;
      mapInitialized.current = false;
    };
  }, [center, zoom, propsWithCoords, onPropertyClick]);

  if (propsWithCoords.length === 0) {
    return (
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        height: '500px',
        background: '#f8f9fa',
        borderRadius: '12px',
        border: '2px dashed #dee2e6',
        padding: '2rem',
        textAlign: 'center',
        color: '#6c757d'
      }}>
        <p>No properties with location data available</p>
        <p style={{ fontSize: '0.9rem', color: '#adb5bd' }}>
          Add latitude and longitude to your properties to see them on the map.
        </p>
      </div>
    );
  }

  return (
    <div style={{
      height: '500px',
      borderRadius: '12px',
      overflow: 'hidden',
      border: '1px solid #e5e7eb',
      position: 'relative',
      background: '#f0f0f0'
    }}>
      {!mapReady && (
        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          height: '100%',
          background: '#f8f9fa',
          fontSize: '16px',
          color: '#6c757d'
        }}>
          <span>Loading map...</span>
        </div>
      )}
      <div
        id="map-container"
        style={{
          height: '100%',
          width: '100%',
          display: mapReady ? 'block' : 'none',
        }}
      />
      <div style={{
        padding: '8px 16px',
        fontSize: '0.85rem',
        color: '#6b7280',
        background: '#f9fafb',
        borderTop: '1px solid #e5e7eb',
        position: 'absolute',
        bottom: 0,
        left: 0,
        right: 0,
        zIndex: 1000,
      }}>
        Showing {propsWithCoords.length} properties on map
        {propsWithCoords.length !== properties.length &&
          ` (${properties.length - propsWithCoords.length} without location)`
        }
      </div>
    </div>
  );
};

export default PropertyMap;