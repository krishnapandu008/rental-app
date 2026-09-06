import React, { useEffect, useMemo, useState } from 'react';
import { ActivityIndicator, ScrollView, Text, TouchableOpacity, View } from 'react-native';
import { WebView } from 'react-native-webview';
import { useNavigation } from '@react-navigation/native';
import { getMapProperties } from '../api/propertyApi';
import { Property } from '../types';
import { colors } from '../styles/common';
import { styles } from './MapScreen.styles';

const defaultRegion: MapRegion = {
  latitude: 12.9716,
  longitude: 77.5946,
  latitudeDelta: 0.35,
  longitudeDelta: 0.35,
};

type MapRegion = {
  latitude: number;
  longitude: number;
  latitudeDelta: number;
  longitudeDelta: number;
};

const buildMapHtml = (properties: Property[], region: MapRegion) => {
  const safeProperties = JSON.stringify(properties).replace(/</g, '\\u003c');
  const safeRegion = JSON.stringify(region);

  return `<!doctype html>
<html>
  <head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <style>
      html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; background: #e7efe9; }
      .leaflet-control-attribution { font-size: 9px; }
      .price-pin { align-items: center; background: #f4511e; border: 2px solid #fff; border-radius: 50% 50% 50% 0; box-shadow: 0 2px 8px rgba(16,42,67,.25); display: flex; height: 40px; justify-content: center; position: relative; transform: rotate(-45deg); width: 40px; }
      .price-pin span { color: #fff; font: 800 11px Arial, sans-serif; transform: rotate(45deg); white-space: nowrap; }
      .popup-title { color: #102a43; font: 700 14px sans-serif; margin-bottom: 5px; }
      .popup-copy { color: #52606d; font: 12px sans-serif; margin-bottom: 5px; }
      .popup-price { color: #d95045; font: 800 13px sans-serif; }
      .popup-action { background: #2f8f83; border: 0; border-radius: 999px; color: #fff; font: 700 11px sans-serif; margin-top: 8px; padding: 7px 12px; }
    </style>
  </head>
  <body>
    <div id="map"></div>
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <script>
      const properties = ${safeProperties};
      const region = ${safeRegion};
      const map = L.map('map', { zoomControl: true, tap: true }).setView(
        [region.latitude, region.longitude],
        Math.max(10, Math.round(Math.log2(360 / Math.max(region.latitudeDelta, 0.01))))
      );
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap contributors'
      }).addTo(map);

      const escapeHtml = (value) => String(value ?? '').replace(/[&<>"']/g, (character) => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'
      }[character]));

      properties.forEach((property) => {
        const rent = Number(property.rent || 0);
        const markerIcon = L.divIcon({
          className: '',
          html: '<div class="price-pin"><span>₹' + Math.round(rent / 1000) + 'k</span></div>',
          iconSize: [41, 50],
          iconAnchor: [20.5, 50]
        });
        const marker = L.marker([property.latitude, property.longitude], { icon: markerIcon }).addTo(map);
        marker.bindPopup(
          '<div class="popup-title">' + escapeHtml(property.title) + '</div>' +
          '<div class="popup-copy">' + escapeHtml(property.location) + '</div>' +
          '<div class="popup-price">₹' + rent.toLocaleString() + ' / month</div>' +
          '<button class="popup-action" onclick="openProperty(' + property.id + ')">View details</button>'
        );
      });

      window.openProperty = (id) => {
        window.ReactNativeWebView.postMessage(JSON.stringify({ type: 'property', id }));
      };
      window.ReactNativeWebView.postMessage(JSON.stringify({ type: 'ready' }));
    </script>
  </body>
</html>`;
};

export default function MapScreen() {
  const [properties, setProperties] = useState<Property[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [mapReady, setMapReady] = useState(false);
  const navigation = useNavigation<any>();

  const loadProperties = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await getMapProperties();
      setProperties(response.data);
    } catch (loadError) {
      console.error('Map properties error:', loadError);
      setError('Could not load properties for the map.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProperties();
  }, []);

  const propertiesWithCoordinates = useMemo(
    () => properties.filter((property) => (
      Number.isFinite(property.latitude) && Number.isFinite(property.longitude)
    )),
    [properties]
  );

  const region = useMemo<MapRegion>(() => {
    if (propertiesWithCoordinates.length === 0) return defaultRegion;

    const latitudes = propertiesWithCoordinates.map((property) => property.latitude as number);
    const longitudes = propertiesWithCoordinates.map((property) => property.longitude as number);
    const minLatitude = Math.min(...latitudes);
    const maxLatitude = Math.max(...latitudes);
    const minLongitude = Math.min(...longitudes);
    const maxLongitude = Math.max(...longitudes);

    return {
      latitude: (minLatitude + maxLatitude) / 2,
      longitude: (minLongitude + maxLongitude) / 2,
      latitudeDelta: Math.max(maxLatitude - minLatitude, 0.08) * 1.6,
      longitudeDelta: Math.max(maxLongitude - minLongitude, 0.08) * 1.6,
    };
  }, [propertiesWithCoordinates]);

  const mapHtml = useMemo(
    () => buildMapHtml(propertiesWithCoordinates, region),
    [propertiesWithCoordinates, region]
  );

  const handleMapMessage = (event: { nativeEvent: { data: string } }) => {
    try {
      const message = JSON.parse(event.nativeEvent.data) as { type: string; id?: number };
      if (message.type === 'ready') setMapReady(true);
      if (message.type === 'property' && message.id !== undefined) {
        const property = propertiesWithCoordinates.find((item) => item.id === message.id);
        if (property) navigation.navigate('Detail', { property });
      }
    } catch (messageError) {
      console.error('Map message error:', messageError);
    }
  };

  if (loading) {
    return (
      <View style={styles.loading}>
        <ActivityIndicator size="large" color={colors.primary} />
        <Text style={styles.loadingText}>Loading map properties...</Text>
      </View>
    );
  }

  if (error) {
    return (
      <View style={styles.loading}>
        <Text style={styles.errorText}>{error}</Text>
        <TouchableOpacity style={styles.retryButton} onPress={loadProperties}>
          <Text style={styles.retryButtonText}>Try again</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.mapStage}>
        <WebView
          style={styles.map}
          originWhitelist={['*']}
          source={{ html: mapHtml }}
          javaScriptEnabled
          domStorageEnabled
          onMessage={handleMapMessage}
          onLoadEnd={() => setMapReady(true)}
        />
        {propertiesWithCoordinates.length === 0 ? (
          <View style={styles.emptyOverlay}>
            <Text style={styles.emptyText}>No properties with location data available.</Text>
          </View>
        ) : null}
        <View style={styles.statusOverlay}>
          <Text style={styles.statusTitle}>
            {mapReady ? `${propertiesWithCoordinates.length} homes on the map` : 'Loading map...'}
          </Text>
        </View>
      </View>
      <View style={styles.bottomSheet}>
        <Text style={styles.mapSummary}>
          Showing {propertiesWithCoordinates.length} properties on map
          {propertiesWithCoordinates.length !== properties.length
            ? ` (${properties.length - propertiesWithCoordinates.length} without location)`
            : ''}
        </Text>
        <Text style={styles.sheetTitle}>Nearby rentals</Text>
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.listOverlay}>
          {propertiesWithCoordinates.map((property) => (
            <TouchableOpacity
              key={`map-list-${property.id}`}
              style={styles.listCard}
              onPress={() => navigation.navigate('Detail', { property })}
            >
              <Text style={styles.listCardTitle} numberOfLines={1}>{property.title}</Text>
              <Text style={styles.listCardLocation} numberOfLines={1}>{property.location}</Text>
              <Text style={styles.listCardPrice}>₹{property.rent.toLocaleString()}</Text>
            </TouchableOpacity>
          ))}
        </ScrollView>
      </View>
    </View>
  );
}