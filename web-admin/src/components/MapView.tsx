import React, { useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import styles from './MapView.module.scss';

// Fix default marker icons
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
});

interface Property {
  id: number;
  title: string;
  location: string;
  rent: number;
  bedrooms: number;
  latitude: number;
  longitude: number;
  imageUrls: string[];
}

interface MapViewProps {
  properties: Property[];
  center: [number, number];
  zoom?: number;
  onPropertyClick: (propertyId: number) => void;
}

const MapView: React.FC<MapViewProps> = ({
  properties,
  center,
  zoom = 12,
  onPropertyClick,
}) => {
  const [isMapReady, setIsMapReady] = useState(false);

  // Create custom marker icon with price
  const createMarkerIcon = (rent: number, isAvailable: boolean = true) => {
    const color = isAvailable ? '#f4511e' : '#6c757d';
    const svg = `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 41 50" width="41" height="50">
        <path d="M20.5 49L1 20.5C1 9.73 9.73 1 20.5 1s19.5 8.73 19.5 19.5L20.5 49z" fill="${color}" stroke="#fff" stroke-width="2"/>
        <text x="20.5" y="28" font-family="Arial" font-size="11" font-weight="bold" fill="#fff" text-anchor="middle">₹${Math.round(rent/1000)}k</text>
      </svg>
    `;
    return L.icon({
      iconUrl: `data:image/svg+xml;base64,${btoa(svg)}`,
      iconSize: [41, 50],
      iconAnchor: [20.5, 50],
      popupAnchor: [0, -45],
    });
  };

  return (
    <div className={styles.mapContainer}>
      <MapContainer
        center={center}
        zoom={zoom}
        className={styles.map}
        whenReady={() => setIsMapReady(true)}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        
        {isMapReady && properties.map((property) => (
          <Marker
            key={property.id}
            position={[property.latitude, property.longitude]}
            icon={createMarkerIcon(property.rent, true)}
            eventHandlers={{
              click: () => onPropertyClick(property.id),
            }}
          >
            <Popup>
              <div className={styles.popup}>
                <h4>{property.title}</h4>
                <p><strong>₹{property.rent.toLocaleString()}</strong> / month</p>
                <p>{property.bedrooms} BHK • {property.location}</p>
                <button 
                  className={styles.popupBtn}
                  onClick={() => onPropertyClick(property.id)}
                >
                  View Details
                </button>
              </div>
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  );
};

export default MapView;