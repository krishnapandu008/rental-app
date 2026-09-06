import React, { useEffect, useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, Keyboard, ScrollView, useWindowDimensions } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { getProperties, PropertyQuery } from '../api/propertyApi';
import { Property } from '../types';
import { callOwner, whatsappOwner } from '../utils/phoneHelper';
import { styles } from './HomeScreen.styles';
import { colors } from '../styles/common';
import PropertyImageThumb from '../components/PropertyImageThumb';

const quickFilters = [
  { label: 'Under ₹10K', icon: '💰', maxPrice: 10000 },
  { label: 'Under ₹15K', icon: '💰', maxPrice: 15000 },
  { label: '1 BHK', icon: '🛏️', bedrooms: 1 },
  { label: '2 BHK', icon: '🛏️', bedrooms: 2 },
  { label: '3 BHK', icon: '🛏️', bedrooms: 3 },
  { label: 'Parking', icon: '🅿️', amenities: ['parking'] },
  { label: 'Furnished', icon: '🛋️', amenities: ['furnished'] },
];

export default function HomeScreen() {
  const [properties, setProperties] = useState<Property[]>([]);
  const [filtered, setFiltered] = useState<Property[]>([]);
  const [search, setSearch] = useState('');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [bedrooms, setBedrooms] = useState('');
  const [propertyType, setPropertyType] = useState('');
  const [sortBy, setSortBy] = useState('newest');
  const [amenities, setAmenities] = useState<string[]>([]);
  const [activeQuickFilters, setActiveQuickFilters] = useState<string[]>([]);
  const [filtersOpen, setFiltersOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigation = useNavigation<any>();
  const { width } = useWindowDimensions();

  useEffect(() => {
    loadProperties();
  }, []);

  const loadProperties = async (query?: PropertyQuery) => {
    setLoading(true);
    setError('');
    try {
      const res = await getProperties(query);
      setProperties(res.data);
      setFiltered(res.data);
    } catch (error: any) {
      setError('Could not load properties. Check your connection and try again.');
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

  const applyFilters = () => {
    const query: PropertyQuery = {};
    if (search.trim()) query.location = search.trim();
    if (minPrice.trim()) query.minPrice = Number(minPrice);
    if (maxPrice.trim()) query.maxPrice = Number(maxPrice);
    if (bedrooms.trim()) query.bedrooms = Number(bedrooms);
    if (propertyType) query.propertyType = propertyType;
    if (sortBy) query.sortBy = sortBy;
    if (amenities.length) query.amenities = amenities;
    Keyboard.dismiss();
    loadProperties(query);
  };

  const clearFilters = () => {
    setSearch('');
    setMinPrice('');
    setMaxPrice('');
    setBedrooms('');
    setPropertyType('');
    setSortBy('newest');
    setAmenities([]);
    setActiveQuickFilters([]);
    setFiltersOpen(false);
    loadProperties();
  };

  const applyQuickFilter = (quickFilter: typeof quickFilters[number]) => {
    const isActive = activeQuickFilters.includes(quickFilter.label);
    const nextActive = isActive
      ? activeQuickFilters.filter((label) => label !== quickFilter.label)
      : [...activeQuickFilters, quickFilter.label];

    setActiveQuickFilters(nextActive);
    if (quickFilter.maxPrice !== undefined) {
      setMaxPrice(isActive ? '' : String(quickFilter.maxPrice));
    }
    if (quickFilter.bedrooms !== undefined) {
      setBedrooms(isActive ? '' : String(quickFilter.bedrooms));
    }
    if (quickFilter.amenities) {
      setAmenities(isActive ? [] : quickFilter.amenities);
    }

    const query: PropertyQuery = {
      location: search.trim() || undefined,
      minPrice: minPrice.trim() ? Number(minPrice) : undefined,
      maxPrice: quickFilter.maxPrice !== undefined
        ? (isActive ? undefined : quickFilter.maxPrice)
        : (maxPrice.trim() ? Number(maxPrice) : undefined),
      bedrooms: quickFilter.bedrooms !== undefined
        ? (isActive ? undefined : quickFilter.bedrooms)
        : (bedrooms.trim() ? Number(bedrooms) : undefined),
      propertyType: propertyType || undefined,
      sortBy,
      amenities: quickFilter.amenities
        ? (isActive ? undefined : quickFilter.amenities)
        : (amenities.length ? amenities : undefined),
    };
    loadProperties(query);
  };

  const toggleAmenity = (amenity: string) => {
    setAmenities((current) => current.includes(amenity)
      ? current.filter((value) => value !== amenity)
      : [...current, amenity]);
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
    <ScrollView
      style={styles.container}
      contentContainerStyle={[
        styles.screenContent,
        { paddingHorizontal: Math.max(12, Math.min(24, width * 0.05)) },
      ]}
      keyboardShouldPersistTaps="handled"
      showsVerticalScrollIndicator={false}
    >
      <View style={styles.heroBanner}>
        <View style={styles.heroBadge}>
          <Text style={styles.heroBadgeDot}>●</Text>
          <Text style={styles.heroBadgeText}>🏠 Find Your Dream Rental</Text>
        </View>
        <Text style={styles.heroTitle}>Discover <Text style={styles.heroHighlight}>Perfect Homes</Text>{'\n'}With AI-Powered Search</Text>
        <Text style={styles.heroSubtitle}>Search, compare, and book verified rentals in your area with smart filters, voice search, and instant inquiries - all in one place.</Text>
        <View style={styles.heroFeatures}>
          <View style={styles.heroFeature}>
            <Text style={styles.heroFeatureIcon}>🎤</Text>
            <View style={styles.heroFeatureCopy}><Text style={styles.heroFeatureText}>Voice Search</Text><Text style={styles.heroFeatureSubtext}>Find properties with AI</Text></View>
          </View>
          <View style={styles.heroFeature}>
            <Text style={styles.heroFeatureIcon}>🔍</Text>
            <View style={styles.heroFeatureCopy}><Text style={styles.heroFeatureText}>Smart Filters</Text><Text style={styles.heroFeatureSubtext}>Price • Location • Amenities</Text></View>
          </View>
          <View style={styles.heroFeature}>
            <Text style={styles.heroFeatureIcon}>❤️</Text>
            <View style={styles.heroFeatureCopy}><Text style={styles.heroFeatureText}>Favorites</Text><Text style={styles.heroFeatureSubtext}>Save & track listings</Text></View>
          </View>
          <View style={styles.heroFeature}>
            <Text style={styles.heroFeatureIcon}>🗺️</Text>
            <View style={styles.heroFeatureCopy}><Text style={styles.heroFeatureText}>Map View</Text><Text style={styles.heroFeatureSubtext}>Visual property search</Text></View>
          </View>
        </View>
      </View>

      <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.quickFiltersRow}>
        {quickFilters.map((quickFilter) => {
          const active = activeQuickFilters.includes(quickFilter.label);
          return (
            <TouchableOpacity
              key={quickFilter.label}
              style={[styles.quickFilterChip, active && styles.quickFilterChipActive]}
              onPress={() => applyQuickFilter(quickFilter)}
            >
              <Text style={styles.quickFilterIcon}>{quickFilter.icon}</Text>
              <Text style={[styles.quickFilterText, active && styles.quickFilterTextActive]}>
                {quickFilter.label}
              </Text>
            </TouchableOpacity>
          );
        })}
      </ScrollView>

      <TouchableOpacity style={styles.mapButton} onPress={() => navigation.navigate('Map')}>
        <Text style={styles.mapButtonIcon}>⌖</Text>
        <Text style={styles.mapButtonText}>Browse the neighborhood map</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.ownerButton} onPress={() => navigation.navigate('OwnerLogin')}>
        <Text style={styles.ownerButtonText}>Owner workspace · Manage listings</Text>
      </TouchableOpacity>

      <View style={styles.searchContainer}>
        <TextInput
          style={styles.searchInput}
          placeholder="Search by location..."
          placeholderTextColor="rgba(255, 255, 255, 0.72)"
          value={search}
          onChangeText={setSearch}
          returnKeyType="search"
        />
        <View style={styles.filterActions}>
          <TouchableOpacity style={styles.filterToggle} onPress={() => setFiltersOpen(!filtersOpen)}>
            <Text style={styles.filterToggleText}>{filtersOpen ? 'Hide filters' : 'Filters'}</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.applyButton} onPress={applyFilters}>
            <Text style={styles.applyButtonText}>Search</Text>
          </TouchableOpacity>
        </View>
        {filtersOpen ? (
          <View style={styles.filtersPanel}>
            <TextInput
              style={styles.filterInput}
              placeholder="₹ Minimum rent"
              placeholderTextColor={colors.textTertiary}
              value={minPrice}
              onChangeText={setMinPrice}
              keyboardType="numeric"
            />
            <TextInput
              style={styles.filterInput}
              placeholder="₹ Maximum rent"
              placeholderTextColor={colors.textTertiary}
              value={maxPrice}
              onChangeText={setMaxPrice}
              keyboardType="numeric"
            />
            <TextInput
              style={styles.filterInput}
              placeholder="🛏 Bedrooms"
              placeholderTextColor={colors.textTertiary}
              value={bedrooms}
              onChangeText={setBedrooms}
              keyboardType="numeric"
            />
            <Text style={styles.filterLabel}>🏠 Property type</Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.optionRow}>
              {[['apartment', '🏢 Apartment'], ['villa', '🏡 Villa'], ['studio', '🛋 Studio'], ['house', '🏠 House']].map(([value, label]) => (
                <TouchableOpacity
                  key={value}
                  style={[styles.optionChip, propertyType === value && styles.optionChipActive]}
                  onPress={() => setPropertyType(propertyType === value ? '' : value)}
                >
                  <Text style={[styles.optionChipText, propertyType === value && styles.optionChipTextActive]}>
                    {label}
                  </Text>
                </TouchableOpacity>
              ))}
            </ScrollView>
            <Text style={styles.filterLabel}>↕ Sort by</Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.optionRow}>
              {[['newest', '🆕 Newest'], ['price_asc', '↗ Price low'], ['price_desc', '↘ Price high']].map(([value, label]) => (
                <TouchableOpacity
                  key={value}
                  style={[styles.optionChip, sortBy === value && styles.optionChipActive]}
                  onPress={() => setSortBy(value)}
                >
                  <Text style={[styles.optionChipText, sortBy === value && styles.optionChipTextActive]}>{label}</Text>
                </TouchableOpacity>
              ))}
            </ScrollView>
            <Text style={styles.filterLabel}>✨ Amenities</Text>
            <View style={styles.amenitiesGrid}>
              {[
                ['parking', '🅿 Parking'], ['furnished', '🛋 Furnished'], ['ac', '❄ AC'], ['security', '🔒 Security'],
                ['gym', '💪 Gym'], ['swimming_pool', '🏊 Pool'], ['garden', '🌿 Garden'], ['wifi', '📶 WiFi'],
                ['pet_friendly', '🐾 Pets'], ['water_supply', '💧 Water'], ['power_backup', '⚡ Power'], ['lift', '🛗 Lift'],
              ].map(([value, label]) => (
                <TouchableOpacity
                  key={value}
                  style={[styles.amenityChip, amenities.includes(value) && styles.optionChipActive]}
                  onPress={() => toggleAmenity(value)}
                >
                  <Text style={[styles.optionChipText, amenities.includes(value) && styles.optionChipTextActive]}>{label}</Text>
                </TouchableOpacity>
              ))}
            </View>
            <TouchableOpacity onPress={clearFilters}>
              <Text style={styles.clearFiltersText}>↺ Clear filters</Text>
            </TouchableOpacity>
          </View>
        ) : null}
      </View>

      {error ? (
        <View style={styles.emptyContainer}>
          <Text style={styles.emptyText}>{error}</Text>
          <TouchableOpacity onPress={() => loadProperties()}>
            <Text style={styles.clearFiltersText}>Try again</Text>
          </TouchableOpacity>
        </View>
      ) : filtered.length > 0 ? (
        <View style={styles.resultsSection}>
          <View style={styles.resultsHeader}>
            <View style={styles.resultsHeadingBlock}>
              <Text style={styles.resultsTitle}>Available homes</Text>
              <Text style={styles.resultsSubtitle}>{filtered.length} rentals found</Text>
            </View>
            <Text style={styles.resultsSort}>↕ {sortBy === 'price_asc' ? 'Price low' : sortBy === 'price_desc' ? 'Price high' : 'Newest'}</Text>
          </View>
          {filtered.map((item) => (
            <View key={item.id}>
              {renderPropertyCard({ item })}
            </View>
          ))}
        </View>
      ) : (
        renderEmpty()
      )}
    </ScrollView>
  );
}