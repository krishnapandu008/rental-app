import React, { useEffect, useState } from 'react';
import { View, Text, FlatList, TextInput, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { getProperties } from '../api/propertyApi';
import { Property } from '../types';
import { callOwner, whatsappOwner } from '../utils/phoneHelper';
import { styles } from './HomeScreen.styles';
import { colors } from '../styles/common';
import PropertyImageThumb from '../components/PropertyImageThumb';

export default function HomeScreen() {
  const [properties, setProperties] = useState<Property[]>([]);
  const [filtered, setFiltered] = useState<Property[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const navigation = useNavigation<any>();

  useEffect(() => {
    loadProperties();
  }, []);

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
    setFiltered(
      properties.filter(p =>
        p.location.toLowerCase().includes(text.toLowerCase())
      )
    );
  };

  const renderPropertyCard = ({ item }: { item: Property }) => (
    <TouchableOpacity 
      activeOpacity={0.7}
      onPress={() => navigation.navigate('Detail', { property: item })}
    >
      <View style={styles.card}>
        {/* Property Image */}
        <PropertyImageThumb 
          imageUrl={item.imageUrls?.[0]} 
          title={item.title}
        />

        {/* Card Content */}
        <View style={styles.cardContent}>
          <View style={styles.cardHeader}>
            <View style={styles.cardTitleRow}>
              <Text style={styles.cardTitle} numberOfLines={2}>{item.title}</Text>
              <View style={styles.priceBadge}>
                <Text style={styles.priceText}>₹{item.rent}</Text>
              </View>
            </View>
          </View>

          <View style={styles.cardMeta}>
            <View style={styles.metaItem}>
              <Text style={styles.metaIcon}>📍</Text>
              <Text style={styles.metaText}>{item.location}</Text>
            </View>
          </View>

          <View style={styles.cardMeta}>
            <View style={styles.metaItem}>
              <Text style={styles.metaIcon}>🛏️</Text>
              <Text style={styles.cardBedrooms}>{item.bedrooms} BHK</Text>
            </View>
          </View>

          {item.description ? (
            <Text style={styles.cardDescription} numberOfLines={2}>{item.description}</Text>
          ) : null}

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
      </View>
    </TouchableOpacity>
  );

  if (loading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color={colors.primary} />
        <Text style={{ marginTop: 12, color: colors.textSecondary }}>Loading properties...</Text>
      </View>
    );
  }

  const renderEmpty = () => (
    <View style={styles.emptyContainer}>
      <Text style={{ fontSize: 24, marginBottom: 8 }}>🏠</Text>
      <Text style={styles.emptyText}>
        {search ? 'No properties found' : 'No properties available'}
      </Text>
      {search ? (
        <TouchableOpacity onPress={() => { setSearch(''); setFiltered(properties); }}>
          <Text style={{ color: colors.primary, marginTop: 12, fontWeight: '600' }}>Clear Search</Text>
        </TouchableOpacity>
      ) : null}
    </View>
  );

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Rental Finder</Text>
        <Text style={styles.headerSubtitle}>Find your perfect home</Text>
      </View>

      <View style={styles.searchContainer}>
        <TextInput
          style={styles.searchInput}
          placeholder="Search by location..."
          placeholderTextColor={colors.textTertiary}
          value={search}
          onChangeText={handleSearch}
        />
      </View>

      {filtered.length > 0 ? (
        <FlatList
          data={filtered}
          keyExtractor={(item) => item.id?.toString() || Math.random().toString()}
          renderItem={renderPropertyCard}
          contentContainerStyle={styles.listContainer}
          scrollEnabled={true}
          showsVerticalScrollIndicator={false}
        />
      ) : (
        renderEmpty()
      )}
    </View>
  );
}