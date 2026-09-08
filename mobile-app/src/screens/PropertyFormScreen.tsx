import React, { useState } from 'react';
import { Alert, Image, KeyboardAvoidingView, Platform, ScrollView, Text, TextInput, TouchableOpacity, useWindowDimensions, View } from 'react-native';
import * as ImagePicker from 'expo-image-picker';
import { createProperty, deleteProperty, deletePropertyImage, PropertyMutation, updateProperty, uploadPropertyImages } from '../api/propertyApi';
import { Property } from '../types';
import { colors } from '../styles/common';

export default function PropertyFormScreen({ route, navigation }: { route: any; navigation: any }) {
  const property = route.params?.property as Property | undefined;
  const { width } = useWindowDimensions();
  const [form, setForm] = useState<PropertyMutation>({
    title: property?.title || '', description: property?.description || '',
    locationId: property?.locationId || 0,
    propertyTypeId: property?.propertyTypeId || (typeof property?.propertyType === 'object' ? property.propertyType.id : 0) || 0,
    rent: property?.rent || 0, bedrooms: property?.bedrooms || 1,
    bathrooms: property?.bathrooms, squareFeet: property?.squareFeet,
    contactNumber: property?.contactNumber || '', available: property?.available ?? true,
    visibility: property?.visibility === 'PRIVATE' || property?.visibility === 'UNLISTED' ? property.visibility : 'PUBLIC',
    latitude: property?.latitude, longitude: property?.longitude,
  });
  const [saving, setSaving] = useState(false);
  const [existingImages, setExistingImages] = useState<string[]>(property?.imageUrls || []);
  const [newImages, setNewImages] = useState<Array<{ uri: string; name: string; type: string }>>([]);
  const [imageBusy, setImageBusy] = useState(false);
  const numericFields = ['rent', 'bedrooms', 'bathrooms', 'squareFeet', 'locationId', 'propertyTypeId', 'latitude', 'longitude'];
  const setField = (key: keyof PropertyMutation, value: string) => setForm((current) => ({
    ...current,
    [key]: numericFields.includes(key) ? (value === '' ? undefined : Number(value)) : value,
  }));
  const renderField = (key: keyof PropertyMutation, label: string, halfWidth = false) => (
    <View key={key} style={halfWidth ? styles.fieldHalf : styles.field}>
      <Text style={styles.label}>{label}</Text>
      <TextInput
        style={styles.input}
        value={String(form[key] ?? '')}
        onChangeText={(value) => setField(key, value)}
        keyboardType={numericFields.includes(key) ? 'numeric' : 'default'}
        multiline={key === 'description'}
      />
    </View>
  );

  const save = async () => {
    const missingFields = [
      !form.title.trim() && 'Title',
      !form.contactNumber.trim() && 'Contact number',
      !form.rent && 'Monthly rent',
      !form.bedrooms && 'Bedrooms',
      !form.locationId && 'Location ID',
      !form.propertyTypeId && 'Property type ID',
    ].filter(Boolean);
    if (missingFields.length) {
      Alert.alert('Complete the form', `Required: ${missingFields.join(', ')}`);
      return;
    }
    setSaving(true);
    try {
      if (property) {
        await updateProperty(property.id, form);
        if (newImages.length) await uploadPropertyImages(property.id, newImages);
      } else {
        await createProperty(form, newImages);
      }
      navigation.goBack();
    } catch (error: any) {
      Alert.alert('Save failed', error.response?.data?.message || `Request failed (${error.response?.status || 'network error'}).`);
    } finally { setSaving(false); }
  };

  const removeProperty = () => {
    if (!property) return;
    Alert.alert('Delete property?', property.title, [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Delete', style: 'destructive', onPress: async () => {
        try {
          await deleteProperty(property.id);
          navigation.navigate('MyProperties', { ownerId: route.params?.ownerId });
        } catch (error: any) {
          Alert.alert('Delete failed', error.response?.data?.message || 'The property could not be deleted.');
        }
      } },
    ]);
  };

  const pickImages = async () => {
    const permission = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permission.granted) { Alert.alert('Photo access needed', 'Allow photo access to upload property images.'); return; }
    const result = await ImagePicker.launchImageLibraryAsync({ mediaTypes: ['images'], allowsMultipleSelection: true, quality: 0.85 });
    if (!result.canceled) setNewImages((current) => [...current, ...result.assets.map((asset, index) => ({ uri: asset.uri, name: asset.fileName || `property-${Date.now()}-${index}.jpg`, type: asset.mimeType || 'image/jpeg' }))]);
  };

  const removeExistingImage = (url: string) => {
    Alert.alert('Delete image?', 'This image will be removed from the property.', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Delete', style: 'destructive', onPress: async () => {
        setImageBusy(true);
        try { await deletePropertyImage(url); setExistingImages((current) => current.filter((image) => image !== url)); }
        catch (error: any) { Alert.alert('Image delete failed', error.response?.data?.message || 'Could not delete image.'); }
        finally { setImageBusy(false); }
      } },
    ]);
  };

  const replaceExistingImage = async (url: string) => {
    const permission = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permission.granted) { Alert.alert('Photo access needed', 'Allow photo access to replace images.'); return; }
    const result = await ImagePicker.launchImageLibraryAsync({ mediaTypes: ['images'], quality: 0.85 });
    if (result.canceled || !result.assets[0] || !property) return;
    const asset = result.assets[0];
    setImageBusy(true);
    try { await deletePropertyImage(url); const uploaded = await uploadPropertyImages(property.id, [{ uri: asset.uri, name: asset.fileName || `property-${Date.now()}.jpg`, type: asset.mimeType || 'image/jpeg' }]); setExistingImages((current) => current.map((image) => image === url ? uploaded.data[0] : image)); }
    catch (error: any) { Alert.alert('Image replace failed', error.response?.data?.message || 'Could not replace image.'); }
    finally { setImageBusy(false); }
  };

  return (
    <KeyboardAvoidingView style={styles.container} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <View style={[styles.topBar, width < 380 && styles.narrowTopBar]}>
        <View style={styles.topBarTitle}>
          <Text style={styles.eyebrow}>OWNER WORKSPACE</Text>
          <Text style={styles.title}>{property ? 'Edit property' : 'Add property'}</Text>
        </View>
        {property ? (
          <View style={styles.topActions}>
            <View style={styles.editState}><Text style={styles.editStateText}>Edit</Text></View>
            <TouchableOpacity style={styles.topDeleteButton} onPress={removeProperty} disabled={saving}>
              <Text style={styles.topDeleteText}>Delete</Text>
            </TouchableOpacity>
          </View>
        ) : null}
      </View>
      <ScrollView contentContainerStyle={[styles.content, width < 380 && styles.narrowContent]} keyboardShouldPersistTaps="handled">
        <Text style={styles.note}>Use the same property fields as web-admin. Location and property type IDs must match backend records.</Text>
        <View style={styles.imageSection}>
          <View style={styles.imageHeading}><Text style={styles.sectionTitle}>Property images</Text><TouchableOpacity style={styles.addImageButton} onPress={pickImages} disabled={imageBusy}><Text style={styles.addImageText}>+ Add photos</Text></TouchableOpacity></View>
          {existingImages.length === 0 && newImages.length === 0 ? <Text style={styles.imageEmpty}>No images selected.</Text> : null}
          <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.imageRow}>
            {existingImages.map((url) => <View style={styles.imageTile} key={url}><Image source={{ uri: url }} style={styles.imagePreview} /><View style={styles.imageActions}><TouchableOpacity onPress={() => replaceExistingImage(url)} disabled={imageBusy}><Text style={styles.imageActionText}>Replace</Text></TouchableOpacity><TouchableOpacity onPress={() => removeExistingImage(url)} disabled={imageBusy}><Text style={styles.imageDeleteText}>Delete</Text></TouchableOpacity></View></View>)}
            {newImages.map((image, index) => <View style={styles.imageTile} key={`${image.uri}-${index}`}><Image source={{ uri: image.uri }} style={styles.imagePreview} /><View style={styles.imageActions}><Text style={styles.newImageText}>New</Text><TouchableOpacity onPress={() => setNewImages((current) => current.filter((_, imageIndex) => imageIndex !== index))}><Text style={styles.imageDeleteText}>Remove</Text></TouchableOpacity></View></View>)}
          </ScrollView>
        </View>
        {renderField('title', 'Title')}
        {renderField('description', 'Description')}
        <View style={styles.fieldRow}>
          {renderField('rent', 'Monthly rent', true)}
          {renderField('bedrooms', 'Bedrooms', true)}
        </View>
        {renderField('contactNumber', 'Contact number')}
        <View style={styles.fieldRow}>
          {renderField('bathrooms', 'Bathrooms', true)}
          {renderField('squareFeet', 'Area (sq ft)', true)}
        </View>
        <View style={styles.fieldRow}>
          {renderField('locationId', 'Location ID', true)}
          {renderField('propertyTypeId', 'Property type ID', true)}
        </View>
        <View style={styles.fieldRow}>
          {renderField('latitude', 'Latitude', true)}
          {renderField('longitude', 'Longitude', true)}
        </View>
        <Text style={styles.label}>Visibility</Text>
        <View style={styles.choiceRow}>
          {(['PUBLIC', 'PRIVATE', 'UNLISTED'] as const).map((value) => (
            <TouchableOpacity key={value} style={[styles.choice, form.visibility === value && styles.choiceActive]} onPress={() => setForm((current) => ({ ...current, visibility: value }))}>
              <Text style={[styles.choiceText, form.visibility === value && styles.choiceTextActive]}>{value}</Text>
            </TouchableOpacity>
          ))}
        </View>
        <TouchableOpacity style={styles.availabilityRow} onPress={() => setForm((current) => ({ ...current, available: !current.available }))}>
          <Text style={styles.label}>Available for rent</Text>
          <Text style={styles.availabilityValue}>{form.available ? 'Yes' : 'No'}</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={save} disabled={saving}><Text style={styles.buttonText}>{saving ? 'Saving...' : property ? 'Update property' : 'Create property'}</Text></TouchableOpacity>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = {
  container: { flex: 1, backgroundColor: colors.background },
  content: { paddingHorizontal: 12, paddingTop: 8, paddingBottom: 24 },
  narrowContent: { paddingHorizontal: 8 },
  topBar: { flexDirection: 'row' as const, alignItems: 'center' as const, justifyContent: 'space-between' as const, gap: 8, minHeight: 52, paddingHorizontal: 12, paddingTop: 7, paddingBottom: 7, borderBottomColor: colors.border, borderBottomWidth: 1, backgroundColor: colors.surfaceLight },
  narrowTopBar: { paddingHorizontal: 8 },
  topBarTitle: { flex: 1, minWidth: 0 },
  eyebrow: { color: colors.primaryDark, fontSize: 9, fontWeight: '800' as const, letterSpacing: 1, marginBottom: 2 },
  title: { color: colors.textPrimary, fontSize: 21, fontWeight: '800' as const, marginBottom: 4 },
  topActions: { flexDirection: 'row' as const, alignItems: 'center' as const, gap: 6, flexShrink: 0 },
  editState: { backgroundColor: colors.primary, borderRadius: 8, minWidth: 58, minHeight: 34, paddingHorizontal: 10, alignItems: 'center' as const, justifyContent: 'center' as const },
  editStateText: { color: colors.surfaceLight, fontSize: 12, fontWeight: '800' as const },
  topDeleteButton: { backgroundColor: '#FDE8E7', borderRadius: 8, minWidth: 62, minHeight: 34, paddingHorizontal: 10, alignItems: 'center' as const, justifyContent: 'center' as const },
  topDeleteText: { color: colors.danger, fontSize: 12, fontWeight: '800' as const },
  note: { color: colors.textSecondary, fontSize: 10, lineHeight: 14, marginBottom: 10 },
  imageSection: { backgroundColor: colors.surfaceLight, borderColor: colors.border, borderWidth: 1, borderRadius: 10, padding: 9, marginBottom: 10 },
  imageHeading: { flexDirection: 'row' as const, alignItems: 'center' as const, justifyContent: 'space-between' as const, marginBottom: 7 },
  sectionTitle: { color: colors.textPrimary, fontSize: 13, fontWeight: '800' as const },
  addImageButton: { backgroundColor: colors.primary, borderRadius: 7, paddingHorizontal: 8, paddingVertical: 6 },
  addImageText: { color: colors.surfaceLight, fontSize: 10, fontWeight: '800' as const },
  imageEmpty: { color: colors.textTertiary, fontSize: 11 },
  imageRow: { gap: 8 },
  imageTile: { width: 116, borderColor: colors.border, borderWidth: 1, borderRadius: 8, overflow: 'hidden' as const, backgroundColor: colors.background },
  imagePreview: { width: 114, height: 82, backgroundColor: colors.surface },
  imageActions: { flexDirection: 'row' as const, alignItems: 'center' as const, justifyContent: 'space-between' as const, paddingHorizontal: 5, paddingVertical: 5, gap: 3 },
  imageActionText: { color: colors.primaryDark, fontSize: 9, fontWeight: '800' as const },
  imageDeleteText: { color: colors.danger, fontSize: 9, fontWeight: '800' as const },
  newImageText: { color: colors.secondaryDark, fontSize: 9, fontWeight: '800' as const },
  field: { marginBottom: 7 },
  fieldRow: { flexDirection: 'row' as const, gap: 8 },
  fieldHalf: { flex: 1, minWidth: 0, marginBottom: 7 },
  label: { color: colors.textSecondary, fontSize: 11, fontWeight: '700' as const, marginBottom: 3 },
  input: { backgroundColor: colors.surfaceLight, borderColor: colors.border, borderWidth: 1, borderRadius: 8, paddingHorizontal: 8, paddingVertical: 6, minHeight: 34, color: colors.textPrimary, fontSize: 12 },
  choiceRow: { flexDirection: 'row' as const, gap: 6, marginBottom: 10, flexWrap: 'wrap' as const },
  choice: { borderColor: colors.border, borderWidth: 1, borderRadius: 8, paddingHorizontal: 10, paddingVertical: 7 },
  choiceActive: { backgroundColor: colors.primary, borderColor: colors.primary },
  choiceText: { color: colors.textSecondary, fontSize: 11, fontWeight: '700' as const },
  choiceTextActive: { color: colors.surfaceLight },
  availabilityRow: { flexDirection: 'row' as const, alignItems: 'center' as const, justifyContent: 'space-between' as const, borderTopColor: colors.border, borderTopWidth: 1, paddingTop: 9, marginBottom: 5 },
  availabilityValue: { color: colors.primaryDark, fontSize: 12, fontWeight: '800' as const },
  button: { backgroundColor: colors.primary, borderRadius: 10, minHeight: 42, alignItems: 'center' as const, justifyContent: 'center' as const, marginTop: 6 },
  buttonText: { color: colors.surfaceLight, fontSize: 13, fontWeight: '800' as const },
};
