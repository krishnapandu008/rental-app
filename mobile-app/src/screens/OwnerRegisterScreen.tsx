import React, { useState } from 'react';
import { ActivityIndicator, Alert, KeyboardAvoidingView, Platform, ScrollView, StyleSheet, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { registerOwner } from '../api/ownerApi';
import { colors } from '../styles/common';

export default function OwnerRegisterScreen({ navigation }: { navigation: any }) {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleRegister = async () => {
    const form = {
      name: name.trim(),
      email: email.trim(),
      phone: phone.trim(),
      password,
    };

    if (!form.name || !form.email || !form.phone || !form.password) {
      Alert.alert('Missing details', 'Enter your name, email, phone, and password.');
      return;
    }

    setLoading(true);
    try {
      await registerOwner(form);
      Alert.alert('Registration successful', 'Your owner account is ready. Please sign in.', [
        { text: 'Sign in', onPress: () => navigation.replace('OwnerLogin') },
      ]);
    } catch (error: any) {
      Alert.alert('Registration failed', error.response?.data?.message || 'Please check your details and try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
        <Text style={styles.eyebrow}>OWNER WORKSPACE</Text>
        <Text style={styles.title}>Create your account</Text>
        <Text style={styles.subtitle}>Register to create and manage your rental listings.</Text>

        <TextInput
          style={styles.input}
          placeholder="Name"
          autoCapitalize="words"
          value={name}
          onChangeText={setName}
        />
        <TextInput
          style={styles.input}
          placeholder="Email"
          autoCapitalize="none"
          keyboardType="email-address"
          value={email}
          onChangeText={setEmail}
        />
        <TextInput
          style={styles.input}
          placeholder="Phone"
          keyboardType="phone-pad"
          value={phone}
          onChangeText={setPhone}
        />
        <TextInput
          style={styles.input}
          placeholder="Password"
          secureTextEntry
          value={password}
          onChangeText={setPassword}
        />

        <TouchableOpacity style={styles.button} onPress={handleRegister} disabled={loading}>
          {loading ? <ActivityIndicator color={colors.surfaceLight} /> : <Text style={styles.buttonText}>Register</Text>}
        </TouchableOpacity>

        <View style={styles.footer}>
          <Text style={styles.footerText}>Already have an account?</Text>
          <TouchableOpacity onPress={() => navigation.replace('OwnerLogin')}>
            <Text style={styles.link}> Sign in</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { flexGrow: 1, justifyContent: 'center', padding: 24 },
  eyebrow: { color: colors.primaryDark, fontSize: 11, fontWeight: '800', letterSpacing: 1.2, marginBottom: 8 },
  title: { color: colors.textPrimary, fontSize: 30, fontWeight: '800', marginBottom: 8 },
  subtitle: { color: colors.textSecondary, fontSize: 14, lineHeight: 20, marginBottom: 24 },
  input: { backgroundColor: colors.surfaceLight, borderColor: colors.border, borderWidth: 1, borderRadius: 14, padding: 14, marginBottom: 12, color: colors.textPrimary },
  button: { backgroundColor: colors.primary, borderRadius: 999, minHeight: 48, alignItems: 'center', justifyContent: 'center', marginTop: 8 },
  buttonText: { color: colors.surfaceLight, fontWeight: '800', fontSize: 15 },
  footer: { flexDirection: 'row', justifyContent: 'center', marginTop: 20 },
  footerText: { color: colors.textSecondary },
  link: { color: colors.primaryDark, fontWeight: '800' },
});
