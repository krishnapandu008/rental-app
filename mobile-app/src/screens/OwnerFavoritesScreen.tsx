import React, { useEffect, useState } from 'react';
import { ActivityIndicator, Alert, ScrollView, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { api } from '../api/client';
import { getProperties } from '../api/propertyApi';
import { Property } from '../types';
import { colors } from '../styles/common';
export default function OwnerFavoritesScreen({ navigation }: { navigation: any }) {
  const [items, setItems] = useState<Property[]>([]); const [loading, setLoading] = useState(true);
  useEffect(() => { const load = async () => { try { const idsResponse = await api.get<number[]>('/favorites'); const ids = idsResponse.data || []; if (!ids.length) return; const response = await getProperties({ size: 100 }); setItems(response.data.filter((item) => ids.includes(item.id))); } catch { Alert.alert('Favorites unavailable', 'Could not load favorites.'); } finally { setLoading(false); } }; load(); }, []);
  if (loading) return <View style={styles.center}><ActivityIndicator color={colors.primary} /></View>;
  return <ScrollView contentContainerStyle={styles.container}><Text style={styles.eyebrow}>OWNER WORKSPACE</Text><Text style={styles.title}>Favorites</Text>{items.length === 0 ? <Text style={styles.empty}>No favorites yet.</Text> : items.map((item) => <TouchableOpacity style={styles.card} key={item.id} onPress={() => navigation.navigate('Detail', { property: item })}><Text style={styles.name}>{item.title}</Text><Text style={styles.meta}>{item.location} · {item.bedrooms} BHK</Text><Text style={styles.price}>₹{item.rent.toLocaleString()} / month</Text></TouchableOpacity>)}</ScrollView>;
}
const styles = StyleSheet.create({ container: { flexGrow: 1, padding: 16, backgroundColor: colors.background }, center: { flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: colors.background }, eyebrow: { color: colors.primaryDark, fontSize: 10, fontWeight: '800', letterSpacing: 1.1 }, title: { color: colors.textPrimary, fontSize: 24, fontWeight: '800', marginTop: 4, marginBottom: 16 }, empty: { color: colors.textSecondary, textAlign: 'center', marginTop: 32 }, card: { backgroundColor: colors.surfaceLight, borderColor: colors.border, borderWidth: 1, borderRadius: 10, padding: 13, marginBottom: 9 }, name: { color: colors.textPrimary, fontSize: 15, fontWeight: '800' }, meta: { color: colors.textSecondary, fontSize: 12, marginTop: 5 }, price: { color: colors.primaryDark, fontSize: 13, fontWeight: '800', marginTop: 7 } });
