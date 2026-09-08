import React, { useEffect, useState } from 'react';
import { Alert, Image, View, Text, TextInput, TouchableOpacity, ActivityIndicator, Keyboard, ScrollView, useWindowDimensions } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { getLocationSuggestions, getProperties, PropertyQuery } from '../api/propertyApi';
import { api } from '../api/client';
import { Property } from '../types';
import { callOwner, whatsappOwner } from '../utils/phoneHelper';
import { styles } from './HomeScreen.styles';
import { colors } from '../styles/common';
import PropertyImageThumb from '../components/PropertyImageThumb';
import { clearOwnerSession, getOwnerSession } from '../api/session';
import type { LoginResponse } from '../api/ownerApi';
import { MaterialCommunityIcons } from '@expo/vector-icons';

const NavIcon = ({ name, size = 21, color = colors.textPrimary }: { name: React.ComponentProps<typeof MaterialCommunityIcons>['name']; size?: number; color?: string }) => (
  <MaterialCommunityIcons name={name} size={size} color={color} />
);

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
  const [menuOpen, setMenuOpen] = useState(false);
  const [ownerSession, setOwnerSession] = useState<LoginResponse | null>(null);
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [suggestionsLoading, setSuggestionsLoading] = useState(false);
  const [favoriteIds, setFavoriteIds] = useState<number[]>([]);
  const navigation = useNavigation<any>();
  const { width } = useWindowDimensions();

  useEffect(() => {
    loadProperties();
    const syncSession = async () => setOwnerSession(await getOwnerSession());
    syncSession();
    const unsubscribe = navigation.addListener('focus', syncSession);
    return unsubscribe;
  }, [navigation]);

  useEffect(() => {
    if (!ownerSession) { setFavoriteIds([]); return; }
    api.get<number[]>('/favorites').then(({ data }) => setFavoriteIds(data || [])).catch(() => setFavoriteIds([]));
  }, [ownerSession]);

  const toggleFavorite = async (propertyId: number) => {
    if (!ownerSession) { navigation.navigate('OwnerLogin'); return; }
    try {
      const { data } = await api.post<boolean>(`/favorites/${propertyId}`);
      setFavoriteIds((current) => data ? [...new Set([...current, propertyId])] : current.filter((id) => id !== propertyId));
    } catch { Alert.alert('Favorites unavailable', 'Could not update favorites.'); }
  };

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

  const updateSearch = async (value: string) => {
    setSearch(value);
    const query = value.trim();
    if (!query) {
      setSuggestions([]);
      return;
    }
    setSuggestionsLoading(true);
    try {
      const response = await getLocationSuggestions(query);
      setSuggestions((response.data || []).slice(0, 6));
    } catch {
      setSuggestions([]);
    } finally {
      setSuggestionsLoading(false);
    }
  };

  const selectSuggestion = (value: string) => {
    setSearch(value);
    setSuggestions([]);
    loadProperties({ location: value, sortBy });
  };

  const clearSearch = () => {
    setSearch('');
    setSuggestions([]);
    loadProperties();
  };

  const startVoiceSearch = () => {
    Alert.alert('Voice search', 'Expo Go does not provide speech recognition. Tap the microphone on the Android keyboard while entering your location.');
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

  const openOwnerFeature = (label: string) => {
    setMenuOpen(false);
    if (!ownerSession) {
      Alert.alert(label, 'Sign in to the owner workspace to continue.', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Sign in', onPress: () => navigation.navigate('OwnerLogin') },
      ]);
      return;
    }
    if (label === 'Add Property') navigation.navigate('PropertyForm', { ownerId: ownerSession.id });
    if (label === 'Favorites') navigation.navigate('OwnerFavorites');
    if (label === 'Inquiries') navigation.navigate('OwnerInquiries');
    if (label === 'Profile') navigation.navigate('OwnerProfile');
    if (label === 'Admin Panel') navigation.navigate('AdminPanel');
  };

  const logout = async () => {
    await clearOwnerSession();
    setOwnerSession(null);
    setMenuOpen(false);
    Alert.alert('Signed out', 'Your mobile owner session has been cleared.');
  };

  const openOwnerWorkspace = async () => {
    const session = await getOwnerSession();
    if (session) {
      navigation.navigate('MyProperties', { ownerId: session.id });
    } else {
      navigation.navigate('OwnerLogin');
    }
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
          compact
        />

        {/* Card Content */}
        <View style={styles.cardContent}>
          <View style={styles.cardHeader}>
            <View style={styles.cardTitleRow}>
              <Text style={styles.cardTitle} numberOfLines={1}>{item.title}</Text>
              <View style={styles.priceBadge}>
                <Text style={styles.priceText}>₹{item.rent.toLocaleString()}</Text>
                <Text style={styles.pricePeriod}>/ month</Text>
              </View>
            </View>
          </View>

          <TouchableOpacity style={styles.favoriteButton} onPress={(event) => { event.stopPropagation(); toggleFavorite(item.id); }} accessibilityLabel="Toggle favorite">
            <NavIcon name={favoriteIds.includes(item.id) ? 'heart' : 'heart-outline'} size={20} color={favoriteIds.includes(item.id) ? colors.danger : colors.primaryDark} />
          </TouchableOpacity>

          <View style={styles.cardMeta}>
            <View style={styles.metaItem}>
              <Text style={styles.metaIcon}>📍</Text>
              <Text style={styles.metaText} numberOfLines={1}>{item.location}</Text>
            </View>
          </View>

          <View style={styles.compactDetails}>
            <Text style={styles.cardBedrooms}>{item.bedrooms} BHK</Text>
            {item.bathrooms ? <Text style={styles.compactDetail}>{item.bathrooms} bath</Text> : null}
            {item.propertyType ? <Text style={styles.compactDetail} numberOfLines={1}>{typeof item.propertyType === 'string' ? item.propertyType : item.propertyType.typeName}</Text> : null}
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
    <View style={styles.appShell}>
      <View style={styles.appBar}>
        <TouchableOpacity style={styles.brand} onPress={() => { setMenuOpen(false); loadProperties(); }}>
          <Image source={require('../../assets/icon.png')} style={styles.brandLogo} />
          <View>
            <Text style={styles.brandName}>ATLAS</Text>
            <Text style={styles.brandSubname}>RENTALS</Text>
          </View>
        </TouchableOpacity>
        <View style={styles.appBarActions}>
          {ownerSession ? (
            <>
              <TouchableOpacity style={styles.iconButton} onPress={() => navigation.navigate('Notifications')} accessibilityLabel="Notifications">
                <NavIcon name="bell-outline" />
              </TouchableOpacity>
              <TouchableOpacity style={styles.profileButton} onPress={() => openOwnerFeature('Profile')} accessibilityLabel="Profile">
                <NavIcon name="account-circle-outline" size={25} color={colors.surfaceLight} />
              </TouchableOpacity>
              <TouchableOpacity style={styles.iconButton} onPress={() => setMenuOpen((current) => !current)} accessibilityLabel="Open menu">
                <NavIcon name={menuOpen ? 'close' : 'menu'} />
              </TouchableOpacity>
            </>
          ) : (
            <>
              <TouchableOpacity style={styles.authLink} onPress={() => navigation.navigate('OwnerLogin')}>
                <Text style={styles.authLinkText}>Login</Text>
              </TouchableOpacity>
              <TouchableOpacity style={styles.registerLink} onPress={() => navigation.navigate('OwnerRegister')}>
                <Text style={styles.registerLinkText}>Register</Text>
              </TouchableOpacity>
            </>
          )}
        </View>
      </View>
      {menuOpen && ownerSession ? (
        <View style={styles.menuPanel}>
          <TouchableOpacity style={styles.menuItem} onPress={() => { setMenuOpen(false); loadProperties(); }}><NavIcon name="view-dashboard-outline" size={19} color={colors.primaryDark} /><Text style={styles.menuText}>Dashboard</Text></TouchableOpacity>
          <TouchableOpacity style={styles.menuItem} onPress={() => openOwnerFeature('Add Property')}><NavIcon name="home-plus-outline" size={19} color={colors.primaryDark} /><Text style={styles.menuText}>Add Property</Text></TouchableOpacity>
          <TouchableOpacity style={styles.menuItem} onPress={() => openOwnerFeature('Favorites')}><NavIcon name="heart-outline" size={19} color={colors.primaryDark} /><Text style={styles.menuText}>Favorites</Text></TouchableOpacity>
          <TouchableOpacity style={styles.menuItem} onPress={() => openOwnerFeature('Inquiries')}><NavIcon name="email-outline" size={19} color={colors.primaryDark} /><Text style={styles.menuText}>Inquiries</Text></TouchableOpacity>
          <TouchableOpacity style={styles.menuItem} onPress={() => openOwnerFeature('Profile')}><NavIcon name="account-outline" size={19} color={colors.primaryDark} /><Text style={styles.menuText}>Profile</Text></TouchableOpacity>
          {ownerSession.role === 'ADMIN' || ownerSession.role === 'SUPER_ADMIN' ? <TouchableOpacity style={styles.menuItem} onPress={() => openOwnerFeature('Admin Panel')}><NavIcon name="cog-outline" size={19} color={colors.primaryDark} /><Text style={styles.menuText}>Admin Panel</Text></TouchableOpacity> : null}
          <TouchableOpacity style={styles.menuItem} onPress={logout}><NavIcon name="logout-variant" size={19} color={colors.primaryDark} /><Text style={styles.menuText}>Logout</Text></TouchableOpacity>
        </View>
      ) : null}
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
        <View style={styles.searchInputWrapper}>
          <NavIcon name="map-marker-outline" size={20} color={colors.accent} />
          <TextInput
            style={styles.searchInput}
            placeholder="Search supports only in Kuppam, Andhra Pradesh"
            placeholderTextColor="rgba(255, 255, 255, 0.72)"
            value={search}
            onChangeText={updateSearch}
            autoCorrect={false}
            returnKeyType="search"
            onSubmitEditing={applyFilters}
          />
          <View style={styles.searchInputActions}>
            {suggestionsLoading ? <ActivityIndicator size="small" color={colors.accent} /> : null}
            {search ? <TouchableOpacity style={styles.searchIconButton} onPress={clearSearch} accessibilityLabel="Clear search"><NavIcon name="close-circle-outline" size={19} color={colors.surfaceLight} /></TouchableOpacity> : null}
            <TouchableOpacity style={styles.searchIconButton} onPress={startVoiceSearch} accessibilityLabel="Voice search"><NavIcon name="microphone-outline" size={20} color={colors.accent} /></TouchableOpacity>
          </View>
        </View>
        {suggestions.length > 0 ? (
          <View style={styles.suggestionsPanel}>
            <Text style={styles.suggestionsHeader}>Suggestions for "{search}"</Text>
            {suggestions.map((suggestion) => <TouchableOpacity key={suggestion} style={styles.suggestionItem} onPress={() => selectSuggestion(suggestion)}><NavIcon name="map-marker-outline" size={17} color={colors.primaryDark} /><Text style={styles.suggestionText}>{suggestion}</Text><NavIcon name="arrow-right" size={16} color={colors.textTertiary} /></TouchableOpacity>)}
          </View>
        ) : null}
        <View style={styles.filterActions}>
          <TouchableOpacity style={styles.filterToggle} onPress={() => setFiltersOpen(!filtersOpen)}>
            <NavIcon name="tune-variant" size={16} color={colors.surfaceLight} />
            <Text style={styles.filterToggleText}>{filtersOpen ? 'Hide filters' : 'Filters'}</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.applyButton} onPress={applyFilters}>
            <NavIcon name="magnify" size={16} color={colors.surfaceLight} />
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
    </View>
  );
}