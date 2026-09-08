import React from 'react';
import { Alert, View, Text, TouchableOpacity, ScrollView, useWindowDimensions } from 'react-native';
import { RouteProp } from '@react-navigation/native';
import { Property } from '../types';
import { callOwner, whatsappOwner } from '../utils/phoneHelper';
import { styles } from './DetailScreen.styles';
import PropertyImageCarousel from '../components/PropertyImageCarousel';
import { deleteProperty } from '../api/propertyApi';

// Type definitions for Route Parameters
type RootStackParamList = {
  Detail: { property: Property; ownerId?: number };
};

type DetailScreenRouteProp = RouteProp<RootStackParamList, 'Detail'>;

interface DetailScreenProps {
  route: DetailScreenRouteProp;
  navigation: any;
}

interface InfoRowProps {
  icon: string;
  label: string;
  value: string | number;
  isLast?: boolean;
  compact?: boolean;
}

const getAmenityName = (amenity: { amenityName?: string; icon?: string } | string) =>
  typeof amenity === 'string' ? amenity : amenity.amenityName || amenity.icon || '';

const InfoRow = ({ 
  icon, 
  label, 
  value, 
  isLast,
  compact,
}: InfoRowProps) => (
  <View style={[styles.infoRow, compact && styles.infoRowCompact, isLast && styles.infoRowLast]}>
    <Text style={styles.infoIcon}>{icon}</Text>
    <View style={styles.infoContent}>
      <Text style={styles.label}>{label}</Text>
      <Text style={styles.value}>{value}</Text>
    </View>
  </View>
);

export default function DetailScreen({ route, navigation }: DetailScreenProps) {
  const { property } = route.params;
  const ownerId = route.params.ownerId;
  const { width } = useWindowDimensions();

  return (
    <View style={styles.screen}>
      <ScrollView contentContainerStyle={[styles.container, styles.scrollContent, width < 380 && styles.narrowContainer]} showsVerticalScrollIndicator={false}>
      {/* Property Image Gallery */}
      <PropertyImageCarousel imageUrls={property?.imageUrls || []} />

      <View style={styles.headerSection}>
        <Text style={styles.title}>{property.title}</Text>
        {property.description ? (
          <Text style={styles.description}>{property.description}</Text>
        ) : null}
      </View>

      <View style={styles.priceContainer}>
        <Text style={styles.priceLabel}>Monthly Rent</Text>
        <Text style={styles.priceValue}>₹{property.rent}</Text>
      </View>

      <View style={styles.infoCard}>
        <View style={styles.infoGrid}>
          <InfoRow icon="📍" label="Location" value={property.location} compact />
          <InfoRow icon="📞" label="Contact Number" value={property.contactNumber} compact />
          <InfoRow icon="🛏️" label="Bedrooms" value={`${property.bedrooms} BHK`} compact />
          {property.bathrooms ? <InfoRow icon="🚿" label="Bathrooms" value={property.bathrooms} compact /> : null}
          {property.squareFeet ? <InfoRow icon="📐" label="Area" value={`${property.squareFeet.toLocaleString()} sq ft`} compact /> : null}
          {property.propertyType ? <InfoRow icon="🏠" label="Property type" value={typeof property.propertyType === 'string' ? property.propertyType : property.propertyType.typeName || ''} compact /> : null}
          {property.owner?.name ? <InfoRow icon="👤" label="Owner" value={property.owner.name} compact /> : null}
          {property.latitude !== undefined && property.longitude !== undefined ? <InfoRow icon="🗺️" label="Coordinates" value={`${property.latitude}, ${property.longitude}`} compact /> : null}
        </View>
      </View>

      <View style={styles.contactActions}>
        <Text style={styles.contactActionsTitle}>Contact owner</Text>
        <View style={styles.buttonRow}>
          <TouchableOpacity
            style={styles.callBtn}
            onPress={() => callOwner(property.contactNumber)}
            activeOpacity={0.7}
          >
            <Text style={styles.btnText}>📞 Call Owner</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={styles.whatsappBtn}
            onPress={() => whatsappOwner(property.contactNumber)}
            activeOpacity={0.7}
          >
            <Text style={styles.btnText}>💬 WhatsApp</Text>
          </TouchableOpacity>
        </View>
      </View>

      {(property.amenities?.length || property.available || property.visibility || property.isActive !== undefined) ? (
        <View style={styles.featuresCard}>
          <Text style={styles.featuresTitle}>Property features</Text>
          <View style={styles.statusRow}>
            {property.available ? <Text style={styles.availableBadge}>Available</Text> : <Text style={styles.unavailableBadge}>Unavailable</Text>}
            {property.visibility ? <Text style={styles.visibilityBadge}>{property.visibility}</Text> : null}
            {property.isActive === false ? <Text style={styles.unavailableBadge}>Inactive</Text> : null}
          </View>
          {property.amenities?.length ? (
            <View style={styles.amenitiesWrap}>
              {property.amenities.map((amenity, index) => (
                <Text key={`${getAmenityName(amenity)}-${index}`} style={styles.amenityBadge}>• {getAmenityName(amenity)}</Text>
              ))}
            </View>
          ) : null}
        </View>
      ) : null}

      {ownerId !== undefined ? (
        <View style={styles.ownerActions}>
          <TouchableOpacity style={styles.editButton} onPress={() => navigation.navigate('PropertyForm', { ownerId, property })}>
            <Text style={styles.editButtonText}>Edit property</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={styles.deleteButton}
            onPress={() => Alert.alert('Delete property?', property.title, [
              { text: 'Cancel', style: 'cancel' },
              { text: 'Delete', style: 'destructive', onPress: async () => {
                try {
                  await deleteProperty(property.id);
                  navigation.navigate('MyProperties', { ownerId });
                } catch (error) {
                  Alert.alert('Delete failed', 'The property could not be deleted.');
                }
              } },
            ])}
          >
            <Text style={styles.deleteButtonText}>Delete property</Text>
          </TouchableOpacity>
        </View>
      ) : null}

      </ScrollView>
    </View>
  );
}