import React, { useState } from 'react';
import { Alert, KeyboardAvoidingView, Platform, ScrollView, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { createProperty, PropertyMutation, updateProperty } from '../api/propertyApi';
import { Property } from '../types';
import { colors } from '../styles/common';

export default function PropertyFormScreen({ route, navigation }: { route: any; navigation: any }) {
  const property = route.params?.property as Property | undefined;
  const [form, setForm] = useState<PropertyMutation>({
    title: property?.title || '', description: property?.description || '', locationId: 0, propertyTypeId: 0,
    rent: property?.rent || 0, bedrooms: property?.bedrooms || 1, contactNumber: property?.contactNumber || '',
    available: property?.available ?? true, visibility: 'PUBLIC', latitude: property?.latitude, longitude: property?.longitude,
  });
  const [saving, setSaving] = useState(false);
  const setField = (key: keyof PropertyMutation, value: string) => setForm((current) => ({ ...current, [key]: ['rent', 'bedrooms', 'locationId', 'propertyTypeId', 'latitude', 'longitude'].includes(key) ? Number(value) : value }));

  const save = async () => {
    if (!form.title.trim() || !form.contactNumber.trim() || !form.rent || !form.locationId || !form.propertyTypeId) {
      Alert.alert('Complete the form', 'Title, rent, contact number, location ID, property type ID, and bedrooms are required.');
      return;
    }
    setSaving(true);
    try {
      if (property) await updateProperty(property.id, form);
      else await createProperty(form);
      navigation.goBack();
    } catch (error: any) {
      Alert.alert('Save failed', error.response?.data?.message || 'Could not save this property.');
    } finally { setSaving(false); }
  };

  return (
    <KeyboardAvoidingView style={styles.container} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
        <Text style={styles.title}>{property ? 'Edit property' : 'Add property'}</Text>
        <Text style={styles.note}>Use the same property fields as web-admin. Location and property type IDs must match backend records.</Text>
        {[
          ['title', 'Title'], ['description', 'Description'], ['rent', 'Monthly rent'], ['bedrooms', 'Bedrooms'],
          ['contactNumber', 'Contact number'], ['locationId', 'Location ID'], ['propertyTypeId', 'Property type ID'],
          ['latitude', 'Latitude'], ['longitude', 'Longitude'],
        ].map(([key, label]) => (
          <View key={key} style={styles.field}>
            <Text style={styles.label}>{label}</Text>
            <TextInput style={styles.input} value={String(form[key as keyof PropertyMutation] ?? '')} onChangeText={(value) => setField(key as keyof PropertyMutation, value)} keyboardType={['rent', 'bedrooms', 'locationId', 'propertyTypeId', 'latitude', 'longitude'].includes(key) ? 'numeric' : 'default'} multiline={key === 'description'} />
          </View>
        ))}
        <TouchableOpacity style={styles.button} onPress={save} disabled={saving}><Text style={styles.buttonText}>{saving ? 'Saving...' : property ? 'Update property' : 'Create property'}</Text></TouchableOpacity>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = {
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: 16, paddingBottom: 32 },
  title: { color: colors.textPrimary, fontSize: 26, fontWeight: '800' as const, marginBottom: 8 },
  note: { color: colors.textSecondary, fontSize: 12, lineHeight: 17, marginBottom: 18 },
  field: { marginBottom: 12 },
  label: { color: colors.textSecondary, fontSize: 12, fontWeight: '700' as const, marginBottom: 5 },
  input: { backgroundColor: colors.surfaceLight, borderColor: colors.border, borderWidth: 1, borderRadius: 12, padding: 12, minHeight: 45, color: colors.textPrimary },
  button: { backgroundColor: colors.primary, borderRadius: 999, minHeight: 48, alignItems: 'center' as const, justifyContent: 'center' as const, marginTop: 8 },
  buttonText: { color: colors.surfaceLight, fontWeight: '800' as const },
};
