import React, { useState } from 'react';
import { ActivityIndicator, Alert, KeyboardAvoidingView, Platform, ScrollView, StyleSheet, Text, TextInput, TouchableOpacity } from 'react-native';
import { registerOwner } from '../api/ownerApi';
import { colors } from '../styles/common';

export default function OwnerRegisterScreen({ navigation }: { navigation: any }) {
  const [form, setForm] = useState({ name: '', email: '', phone: '', password: '' });
  const [loading, setLoading] = useState(false);

  const setField = (field: keyof typeof form, value: string) => setForm((current) => ({ ...current, [field]: value }));

  const handleRegister = async () => {
    if (!form.name.trim() || !form.email.trim() || !form.phone.trim() || !form.password) {
      Alert.alert('Missing details', 'Enter your name, email, phone, and password.');
      return;
    }
    setLoading(true);
    try {
      await registerOwner({ ...form, name: form.name.trim(), email: form.email.trim(), phone: form.phone.trim() });
      Alert.alert('Registration complete', 'Your owner account is ready. Please sign in.', [
        { text: 'Sign in', onPress: () => navigation.replace('OwnerLogin') },
      ]);
    } catch (error: any) {
      Alert.alert('Registration failed', error.response?.data?.message || 'Could not create your account.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView style={styles.container} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
        <Text style={styles.eyebrow}>OWNER WORKSPACE</Text>
        <Text style={styles.title}>Create your account</Text>
        <Text style={styles.subtitle}>Register to create, edit, and manage your property listings.</Text>
        <TextInput style={styles.input} placeholder="Name" value={form.name} onChangeText={(value) => setField('name', value)} />
        <TextInput style={styles.input} placeholder="Email" autoCapitalize="none" keyboardType="email-address" value={form.email} onChangeText={(value) => setField('email', value)} />
        <TextInput style={styles.input} placeholder="Phone" keyboardType="phone-pad" value={form.phone} onChangeText={(value) => setField('phone', value)} />
        <TextInput style={styles.input} placeholder="Password" secureTextEntry value={form.password} onChangeText={(value) => setField('password', value)} />
        <TouchableOpacity style={styles.button} onPress={handleRegister} disabled={loading}>
          {loading ? <ActivityIndicator color={colors.surfaceLight} /> : <Text style={styles.buttonText}>Register</Text>}
        </TouchableOpacity>
        <TouchableOpacity style={styles.linkButton} onPress={() => navigation.goBack()} disabled={loading}>
          <Text style={styles.linkText}>Already have an account? Sign in</Text>
        </TouchableOpacity>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { flexGrow: 1, padding: 24, justifyContent: 'center' },
  eyebrow: { color: colors.primaryDark, fontSize: 11, fontWeight: '800', letterSpacing: 1.2, marginBottom: 8 },
  title: { color: colors.textPrimary, fontSize: 30, fontWeight: '800', marginBottom: 8 },
  subtitle: { color: colors.textSecondary, fontSize: 14, lineHeight: 20, marginBottom: 24 },
  input: { backgroundColor: colors.surfaceLight, borderColor: colors.border, borderWidth: 1, borderRadius: 14, padding: 14, marginBottom: 12, color: colors.textPrimary },
  button: { backgroundColor: colors.primary, borderRadius: 999, minHeight: 48, alignItems: 'center', justifyContent: 'center', marginTop: 8 },
  buttonText: { color: colors.surfaceLight, fontWeight: '800', fontSize: 15 },
  linkButton: { alignItems: 'center', paddingVertical: 16 },
  linkText: { color: colors.primaryDark, fontWeight: '700' },
});