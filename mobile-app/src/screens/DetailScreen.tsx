import React from 'react';
import { View, Text, TouchableOpacity, ScrollView } from 'react-native';
import { Property } from '../types';
import { callOwner, whatsappOwner } from '../utils/phoneHelper';
import { styles } from './DetailScreen.styles';


export default function DetailScreen({ route }: any) {
  const property: Property = route.params.property;

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>{property.title}</Text>
      {property.description && <Text style={styles.description}>{property.description}</Text>}
      <View style={styles.infoCard}>
        <Text style={styles.label}>📍 Location</Text>
        <Text style={styles.value}>{property.location}</Text>

        <Text style={styles.label}>💰 Rent (per month)</Text>
        <Text style={styles.value}>₹{property.rent}</Text>

        <Text style={styles.label}>🛏️ Bedrooms</Text>
        <Text style={styles.value}>{property.bedrooms} BHK</Text>

        <Text style={styles.label}>📞 Contact Number</Text>
        <Text style={styles.value}>{property.contactNumber}</Text>
      </View>
      <View style={styles.buttonRow}>
        <TouchableOpacity style={styles.callBtn} onPress={() => callOwner(property.contactNumber)}>
          <Text style={styles.btnText}>📞 Call Owner</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.whatsappBtn} onPress={() => whatsappOwner(property.contactNumber)}>
          <Text style={styles.btnText}>💬 WhatsApp</Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
} 


