 import { StyleSheet } from 'react-native';

export const colors = {
  // Primary gradient colors
  primary: '#FF6B6B',
  primaryDark: '#EE5A6F',
  
  // Secondary gradient
  secondary: '#4ECDC4',
  secondaryDark: '#45B7B0',
  
  // Accent colors
  accent: '#FFD93D',
  danger: '#E63946',
  success: '#06A77D',
  warning: '#FB5607',
  
  // WhatsApp branding
  whatsapp: '#25D366',
  
  // Neutral colors
  background: '#F8F9FA',
  surfaceLight: '#FFFFFF',
  surface: '#F5F7FA',
  
  // Text colors
  textPrimary: '#1A1A2E',
  textSecondary: '#6B7280',
  textTertiary: '#9CA3AF',
  
  // Border & Shadow
  border: '#E5E7EB',
  shadow: '#000000',
  lightGray: '#E0E0E0',
  
  // Gradient pairs
  gradients: {
    primary: ['#FF6B6B', '#EE5A6F'],
    secondary: ['#4ECDC4', '#45B7B0'],
    warm: ['#FFD93D', '#FB5607'],
  },
};

export const spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  xxl: 24,
};

export const typography = StyleSheet.create({
  title: {
    fontSize: 28,
    fontWeight: '700',
    color: colors.textPrimary,
  },
  heading: {
    fontSize: 20,
    fontWeight: '600',
    color: colors.textPrimary,
  },
  subheading: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.textSecondary,
  },
  body: {
    fontSize: 14,
    fontWeight: '400',
    color: colors.textPrimary,
  },
  bodySmall: {
    fontSize: 13,
    fontWeight: '400',
    color: colors.textSecondary,
  },
  caption: {
    fontSize: 12,
    fontWeight: '400',
    color: colors.textTertiary,
  },
});

export const shadows = {
  sm: {
    elevation: 2,
    shadowColor: colors.shadow,
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.08,
    shadowRadius: 2,
  },
  md: {
    elevation: 4,
    shadowColor: colors.shadow,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  lg: {
    elevation: 8,
    shadowColor: colors.shadow,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 8,
  },
};

export const borderRadius = {
  sm: 4,
  md: 8,
  lg: 12,
  xl: 16,
  full: 9999,
};
