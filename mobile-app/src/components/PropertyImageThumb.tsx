import React, { useState } from 'react';
import { 
  View, 
  Image, 
  ActivityIndicator,
  StyleSheet,
  Text 
} from 'react-native';
import { colors, spacing } from '../styles/common';

interface PropertyImageThumbProps {
  imageUrl?: string;
  title?: string;
}

export default function PropertyImageThumb({ imageUrl, title }: PropertyImageThumbProps) {
  const [loading, setLoading] = useState(true);
  const [hasError, setHasError] = useState(false);

  // No image provided - show placeholder
  if (!imageUrl) {
    return (
      <View style={styles.container}>
        <View style={styles.placeholderContainer}>
          <Text style={styles.placeholderIcon}>🏠</Text>
        </View>
      </View>
    );
  }

  // Image failed to load - show placeholder
  if (hasError) {
    return (
      <View style={styles.container}>
        <View style={styles.placeholderContainer}>
          <Text style={styles.placeholderIcon}>🏠</Text>
        </View>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      {loading && (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="small" color={colors.primary} />
        </View>
      )}
      <Image
        source={{ uri: imageUrl }}
        style={styles.image}
        onLoadEnd={() => setLoading(false)}
        onError={() => {
          setLoading(false);
          setHasError(true);
        }}
        resizeMode="cover"
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: '100%',
    aspectRatio: 1.5,
    minHeight: 180,
    borderTopLeftRadius: 18,
    borderTopRightRadius: 18,
    backgroundColor: colors.surface,
    overflow: 'hidden',
  },
  image: {
    width: '100%',
    height: '100%',
  },
  loadingContainer: {
    ...StyleSheet.absoluteFill,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: colors.surface,
    zIndex: 1,
  },
  placeholderContainer: {
    width: '100%',
    height: '100%',
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: colors.surface,
  },
  placeholderIcon: {
    fontSize: 48,
  },
});
