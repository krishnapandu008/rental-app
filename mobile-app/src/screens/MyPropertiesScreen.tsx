import React, { useCallback, useEffect, useState } from 'react';
import { ActivityIndicator, Alert, FlatList, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { deleteProperty, getOwnerProperties } from '../api/propertyApi';
import { Property } from '../types';
import { colors } from '../styles/common';

export default function MyPropertiesScreen({ route, navigation }: { route: any; navigation: any }) {
  const ownerId = route.params?.ownerId as number;
  const [properties, setProperties] = useState<Property[]>([]);
  const [loading, setLoading] = useState(true);

  const loadProperties = useCallback(async () => {
    try {
      setLoading(true);
      const response = await getOwnerProperties(ownerId);
      setProperties(response.data);
    } catch (error: any) {
      Alert.alert('Could not load listings', error.response?.status === 401 ? 'Please sign in again.' : 'Try again shortly.');
    } finally {
      setLoading(false);
    }
  }, [ownerId]);

  useEffect(() => {
    const unsubscribe = navigation.addListener('focus', loadProperties);
    return unsubscribe;
  }, [loadProperties, navigation]);

  const removeProperty = (property: Property) => {
    Alert.alert('Delete property?', property.title, [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Delete', style: 'destructive', onPress: async () => {
        try {
          await deleteProperty(property.id);
          setProperties((current) => current.filter((item) => item.id !== property.id));
        } catch (error) {
          Alert.alert('Delete failed', 'The property could not be deleted.');
        }
      } },
    ]);
  };

  if (loading) return <View style={styles.center}><ActivityIndicator color={colors.primary} /></View>;

  return (
    <View style={styles.container}>
      <View style={styles.headingRow}>
        <View><Text style={styles.eyebrow}>OWNER WORKSPACE</Text><Text style={styles.title}>My properties</Text></View>
        <TouchableOpacity style={styles.addButton} onPress={() => navigation.navigate('PropertyForm', { ownerId })}><Text style={styles.addButtonText}>+ Add</Text></TouchableOpacity>
      </View>
      {properties.length === 0 ? <Text style={styles.empty}>No properties yet. Add your first listing.</Text> : (
        <FlatList
          data={properties}
          keyExtractor={(item) => String(item.id)}
          contentContainerStyle={styles.list}
          renderItem={({ item }) => (
            <View style={styles.card}>
              <Text style={styles.cardTitle} numberOfLines={2}>{item.title}</Text>
              <Text style={styles.meta}>{item.location} · {item.bedrooms} BHK</Text>
              <Text style={styles.price}>₹{item.rent.toLocaleString()} / month</Text>
              <View style={styles.actions}>
                <TouchableOpacity style={styles.editButton} onPress={() => navigation.navigate('PropertyForm', { ownerId, property: item })}><Text style={styles.editText}>Edit</Text></TouchableOpacity>
                <TouchableOpacity style={styles.deleteButton} onPress={() => removeProperty(item)}><Text style={styles.deleteText}>Delete</Text></TouchableOpacity>
              </View>
            </View>
          )}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 16, backgroundColor: colors.background },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: colors.background },
  headingRow: { flexDirection: 'row' as const, alignItems: 'center' as const, justifyContent: 'space-between' as const, marginBottom: 16 },
  eyebrow: { color: colors.primaryDark, fontSize: 10, fontWeight: '800' as const, letterSpacing: 1.1 },
  title: { color: colors.textPrimary, fontSize: 25, fontWeight: '800' as const, marginTop: 4 },
  addButton: { backgroundColor: colors.primary, borderRadius: 999, paddingHorizontal: 16, paddingVertical: 10 },
  addButtonText: { color: colors.surfaceLight, fontWeight: '800' as const },
  list: { paddingBottom: 24 },
  empty: { color: colors.textSecondary, textAlign: 'center' as const, marginTop: 48 },
  card: { backgroundColor: colors.surfaceLight, borderColor: colors.border, borderWidth: 1, borderRadius: 18, padding: 16, marginBottom: 12 },
  cardTitle: { color: colors.textPrimary, fontSize: 17, fontWeight: '800' as const },
  meta: { color: colors.textSecondary, fontSize: 13, marginTop: 8 },
  price: { color: colors.primaryDark, fontWeight: '800' as const, fontSize: 16, marginTop: 8 },
  actions: { flexDirection: 'row' as const, gap: 8, marginTop: 14 },
  editButton: { flex: 1, borderColor: colors.primary, borderWidth: 1, borderRadius: 999, paddingVertical: 9, alignItems: 'center' as const },
  editText: { color: colors.primaryDark, fontWeight: '800' as const },
  deleteButton: { flex: 1, backgroundColor: '#FDE8E7', borderRadius: 999, paddingVertical: 9, alignItems: 'center' as const },
  deleteText: { color: colors.danger, fontWeight: '800' as const },
});
