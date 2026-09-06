import React, { useState } from 'react';
import { ActivityIndicator, Alert, StyleSheet, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { loginOwner } from '../api/ownerApi';
import { colors } from '../styles/common';

export default function OwnerLoginScreen({ navigation }: { navigation: any }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
    if (!email.trim() || !password) {
      Alert.alert('Missing details', 'Enter your email and password.');
      return;
    }
    setLoading(true);
    try {
      const response = await loginOwner({ email: email.trim(), password });
      navigation.replace('MyProperties', { ownerId: response.data.id });
    } catch (error: any) {
      Alert.alert('Login failed', error.response?.data?.message || 'Check your credentials and try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.eyebrow}>OWNER WORKSPACE</Text>
      <Text style={styles.title}>Manage your rentals</Text>
      <Text style={styles.subtitle}>Sign in to create, edit, and remove your property listings.</Text>
      <TextInput style={styles.input} placeholder="Email" autoCapitalize="none" keyboardType="email-address" value={email} onChangeText={setEmail} />
      <TextInput style={styles.input} placeholder="Password" secureTextEntry value={password} onChangeText={setPassword} />
      <TouchableOpacity style={styles.button} onPress={handleLogin} disabled={loading}>
        {loading ? <ActivityIndicator color={colors.surfaceLight} /> : <Text style={styles.buttonText}>Sign in</Text>}
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 24, justifyContent: 'center', backgroundColor: colors.background },
  eyebrow: { color: colors.primaryDark, fontSize: 11, fontWeight: '800', letterSpacing: 1.2, marginBottom: 8 },
  title: { color: colors.textPrimary, fontSize: 30, fontWeight: '800', marginBottom: 8 },
  subtitle: { color: colors.textSecondary, fontSize: 14, lineHeight: 20, marginBottom: 24 },
  input: { backgroundColor: colors.surfaceLight, borderColor: colors.border, borderWidth: 1, borderRadius: 14, padding: 14, marginBottom: 12, color: colors.textPrimary },
  button: { backgroundColor: colors.primary, borderRadius: 999, minHeight: 48, alignItems: 'center', justifyContent: 'center', marginTop: 8 },
  buttonText: { color: colors.surfaceLight, fontWeight: '800', fontSize: 15 },
});
