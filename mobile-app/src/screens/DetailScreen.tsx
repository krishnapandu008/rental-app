import React from 'react';
import { View, Text, TouchableOpacity, ScrollView } from 'react-native';
import { RouteProp } from '@react-navigation/native';
import { Property } from '../types';
import { callOwner, whatsappOwner } from '../utils/phoneHelper';
import { styles } from './DetailScreen.styles';
import PropertyImageCarousel from '../components/PropertyImageCarousel';

// Type definitions for Route Parameters
type RootStackParamList = {
  Detail: { property: Property };
};

type DetailScreenRouteProp = RouteProp<RootStackParamList, 'Detail'>;

interface DetailScreenProps {
  route: DetailScreenRouteProp;
}

interface InfoRowProps {
  icon: string;
  label: string;
  value: string | number;
  isLast?: boolean;
}

const InfoRow = ({ 
  icon, 
  label, 
  value, 
  isLast 
}: InfoRowProps) => (
  <View style={[styles.infoRow, isLast && styles.infoRowLast]}>
    <Text style={styles.infoIcon}>{icon}</Text>
    <View style={styles.infoContent}>
      <Text style={styles.label}>{label}</Text>
      <Text style={styles.value}>{value}</Text>
    </View>
  </View>
);

export default function DetailScreen({ route }: DetailScreenProps) {
  const { property } = route.params;

  return (
    <ScrollView contentContainerStyle={styles.container} showsVerticalScrollIndicator={false}>
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
        <InfoRow 
          icon="📍" 
          label="Location" 
          value={property.location} 
        />
        <InfoRow 
          icon="🛏️" 
          label="Bedrooms" 
          value={`${property.bedrooms} BHK`} 
        />
        <InfoRow 
          icon="📞" 
          label="Contact Number" 
          value={property.contactNumber} 
          isLast
        />
      </View>

      <View style={styles.contactCard}>
        <Text style={styles.contactLabel}>Owner Contact</Text>
        <Text style={styles.contactNumber}>{property.contactNumber}</Text>
      </View>

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
    </ScrollView>
  );
}