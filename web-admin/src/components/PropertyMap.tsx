import React, { useEffect, useState, useRef } from 'react';
import { Property } from '../types';

interface PropertyMapProps {
  properties: Property[];
  center: [number, number];
  zoom: number;
  onPropertyClick: (id: number) => void;
}

const PropertyMap: React.FC<PropertyMapProps> = ({ properties, center, zoom, onPropertyClick }) => {
  const [mapReady, setMapReady] = useState(false);
  const mapInitialized = useRef(false);
  const mapRef = useRef<any>(null);
  const markersRef = useRef<any[]>([]);

  const propsWithCoords = properties.filter(
    (property): property is Property & { latitude: number; longitude: number } =>
      property.latitude !== undefined && property.longitude !== undefined &&
      property.latitude !== null && property.longitude !== null
  );

  useEffect(() => {
    if (mapInitialized.current) return;
    let isMounted = true;

    const loadMap = async () => {
      try {
        const L = (await import('leaflet')).default;
        await import('leaflet/dist/leaflet.css');
        if (!isMounted) return;

        const container = document.getElementById('map-container');
        if (!container) return;

        const map = L.map(container, { center, zoom, zoomControl: true, fadeAnimation: true });
        const streetLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        }).addTo(map);
        const satelliteLayer = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
          attribution: 'Tiles &copy; Esri',
        });
        const terrainLayer = L.tileLayer('https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png', {
          attribution: 'Map data &copy; OpenStreetMap contributors, SRTM | Map style &copy; OpenTopoMap',
        });
        L.control.layers({
          Street: streetLayer,
          Satellite: satelliteLayer,
          Terrain: terrainLayer,
        }, undefined, { collapsed: true, position: 'topright' }).addTo(map);

        const createPriceMarker = (rent: number) => {
          const canvas = document.createElement('canvas');
          canvas.width = 41;
          canvas.height = 50;
          const context = canvas.getContext('2d')!;
          context.fillStyle = '#f4511e';
          context.strokeStyle = '#ffffff';
          context.lineWidth = 2;
          context.beginPath();
          context.moveTo(20.5, 49);
          context.lineTo(1, 20.5);
          context.quadraticCurveTo(1, 1, 20.5, 1);
          context.quadraticCurveTo(40, 1, 40, 20.5);
          context.lineTo(20.5, 49);
          context.closePath();
          context.fill();
          context.stroke();
          context.fillStyle = '#ffffff';
          context.font = 'bold 11px Arial';
          context.textAlign = 'center';
          context.textBaseline = 'middle';
          context.fillText(`₹${Math.round(rent / 1000)}k`, 20.5, 28);
          return L.icon({
            iconUrl: canvas.toDataURL('image/png'),
            iconSize: [41, 50],
            iconAnchor: [20.5, 50],
            popupAnchor: [0, -45],
          });
        };

        const markers: any[] = [];
        propsWithCoords.forEach((property) => {
          const marker = L.marker([property.latitude, property.longitude], {
            icon: createPriceMarker(property.rent),
          }).addTo(map).bindPopup(`
            <div style="padding: 4px; min-width: 180px;">
              <strong>${property.title}</strong><br />
              ${property.location?.displayName || 'Location unavailable'}<br />
              <strong>₹${property.rent.toLocaleString()}</strong> / month<br />
              ${property.bedrooms} BHK<br />
              <button style="margin-top: 8px; padding: 4px 16px; background: #f4511e; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 12px; font-weight: 600;" onclick="window.__mapPropertyClick(${property.id})">
                View Details
              </button>
            </div>
          `);
          marker.on('mouseover', () => marker.openPopup());
          markers.push(marker);
        });

        (window as any).__mapPropertyClick = (id: number) => onPropertyClick(id);
        mapRef.current = map;
        markersRef.current = markers;
        setMapReady(true);
        mapInitialized.current = true;

        if (propsWithCoords.length > 1) {
          map.fitBounds(L.featureGroup(markers).getBounds(), { padding: [50, 50] });
        }
      } catch (error) {
        console.error('Failed to load Leaflet:', error);
      }
    };

    loadMap();
    return () => {
      isMounted = false;
      if (mapRef.current) mapRef.current.remove();
      mapRef.current = null;
      markersRef.current = [];
      delete (window as any).__mapPropertyClick;
      mapInitialized.current = false;
    };
  }, [center, zoom, propsWithCoords, onPropertyClick]);

  if (propsWithCoords.length === 0) {
    return <div style={{ display: 'grid', placeItems: 'center', height: '500px', background: '#f8f9fa', color: '#6c757d' }}>No properties with location data available</div>;
  }

  return (
    <div style={{ height: '500px', borderRadius: '12px', overflow: 'hidden', border: '1px solid #e5e7eb', position: 'relative', background: '#f0f0f0' }}>
      {!mapReady && <div style={{ display: 'grid', placeItems: 'center', height: '100%', background: '#f8f9fa', color: '#6c757d' }}>Loading map...</div>}
      <div id="map-container" style={{ height: '100%', width: '100%', display: mapReady ? 'block' : 'none' }} />
      <div style={{ padding: '8px 16px', fontSize: '0.85rem', color: '#6b7280', background: '#f9fafb', position: 'absolute', bottom: 0, left: 0, right: 0, zIndex: 1000 }}>
        Showing {propsWithCoords.length} properties on map
        {propsWithCoords.length !== properties.length && ` (${properties.length - propsWithCoords.length} without location)`}
      </div>
    </div>
  );
};

export default PropertyMap;