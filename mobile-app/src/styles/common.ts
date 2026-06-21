 import { StyleSheet } from 'react-native';

export const colors = {
  primary: '#f4511e',
  secondary: '#28a745',
  whatsapp: '#25D366',
  background: '#f5f5f5',
  white: '#fff',
  black: '#000',
  gray: '#666',
  lightGray: '#ccc',
  cardShadow: '#ddd',
};

export const spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
};

export const typography = StyleSheet.create({
  title: {
    fontSize: 24,
    fontWeight: 'bold',
  },
  heading: {
    fontSize: 18,
    fontWeight: '600',
  },
  body: {
    fontSize: 14,
  },
  caption: {
    fontSize: 12,
    color: colors.gray,
  },
});
