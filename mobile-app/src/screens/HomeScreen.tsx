import React, { useEffect, useState } from 'react';
import { View, Text, FlatList, TextInput, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { getProperties } from '../api/propertyApi';
import { Property } from '../types';
import { callOwner, whatsappOwner } from '../utils/phoneHelper';
import { styles } from './HomeScreen.styles';  // <-- import external styles

export default function HomeScreen() {
  const [properties, setProperties] = useState<Property[]>([]);
  const [filtered, setFiltered] = useState<Property[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const navigation = useNavigation<any>();

  useEffect(() => { loadProperties(); }, []);

  const loadProperties = async () => {
    try {
      const res = await getProperties();
      setProperties(res.data);
      setFiltered(res.data);
    } catch (error: any) {
      console.error('API Error Details:', {
        message: error.message,
        code: error.code,
        status: error.response?.status,
        statusText: error.response?.statusText,
        url: error.config?.url,
        data: error.response?.data,
      });
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (text: string) => {
    setSearch(text);
    setFiltered(properties.filter(p => p.location.toLowerCase().includes(text.toLowerCase())));
  };

  if (loading) return <View style={styles.center}><ActivityIndicator size="large" /></View>;

  return (
    <View style={styles.container}>
      <TextInput
        style={styles.searchInput}
        placeholder="Search by location (e.g., Kuppam)"
        value={search}
        onChangeText={handleSearch}
      />
      <FlatList
        data={filtered}
        keyExtractor={item => item.id.toString()}
        renderItem={({ item }) => (
          <TouchableOpacity onPress={() => navigation.navigate('Detail', { property: item })}>
            <View style={styles.card}>
              <Text style={styles.title}>{item.title}</Text>
              <Text>{item.location}</Text>
              <Text style={styles.rent}>₹{item.rent}/month</Text>
              <Text>{item.bedrooms} BHK</Text>
              <View style={styles.buttonRow}>
                <TouchableOpacity
                  style={styles.callBtn}
                  onPress={(e) => { e.stopPropagation(); callOwner(item.contactNumber); }}
                >
                  <Text style={styles.btnText}>📞 Call</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={styles.whatsappBtn}
                  onPress={(e) => { e.stopPropagation(); whatsappOwner(item.contactNumber); }}
                >
                  <Text style={styles.btnText}>💬 WhatsApp</Text>
                </TouchableOpacity>
              </View>
            </View>
          </TouchableOpacity>
        )}
      />
    </View>
  );
}