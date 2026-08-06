import { api } from './client';
import { Property, PageResponse, VoiceSearchResponse } from '../types';
import { SEND_AMENITIES_AS_CSV } from '../utils/constants';

export const getProperties = (params?: {
  location?: string;
  minPrice?: number;
  maxPrice?: number;
  bedrooms?: number;
  propertyType?: string;
  amenities?: string[];
  sortBy?: string;
  page?: number;
  size?: number;
}) => {
  const safeParams = { ...(params || {}) } as any;
  if (Array.isArray(safeParams.amenities)) {
    if (SEND_AMENITIES_AS_CSV) safeParams.amenities = safeParams.amenities.join(',');
    // If not CSV, leave as array and let axios serialize (or backend handle repeated params)
  }
  return api.get<PageResponse<Property>>('/properties', { params: safeParams });
};

export const getMyProperties = (ownerId: number) =>
  api.get<Property[]>(`/properties/owner/${ownerId}`);

export const addProperty = (
  data: Omit<Property, 'id' | 'available' | 'imageUrls' | 'ownerId' | 'isActive'>,
  images?: File[]
) => {
  const formData = new FormData();

  const payload = {
    title: data.title,
    description: data.description,
    location: data.location,
    rent: data.rent,
    bedrooms: data.bedrooms,
    contactNumber: data.contactNumber,
    visibility: data.visibility || 'PUBLIC',
    latitude: data.latitude,
    longitude: data.longitude,
    amenities: data.amenities,
  };
  formData.append('dto', new Blob([JSON.stringify(payload)], { type: 'application/json' }));

  if (images && images.length > 0) {
    images.forEach((file) => formData.append('images', file));
  }

  return api.post<Property>('/properties', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

export const updateProperty = (
  id: number,
  data: Partial<
    Pick<Property, 'title' | 'description' | 'location' | 'rent' | 'bedrooms' | 'contactNumber' | 'available' | 'visibility' | 'latitude' | 'longitude' | 'amenities'>
  >
) => api.put<Property>(`/properties/${id}`, data);

export const uploadImages = (id: number, images: File[]) => {
  const formData = new FormData();
  images.forEach((file) => formData.append('images', file));
  return api.post<string[]>(`/properties/${id}/images`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

export const deleteProperty = (id: number) => api.delete(`/properties/${id}`);

export const togglePropertyActive = (id: number, active: boolean) =>
  api.patch(`/properties/admin/${id}/active?active=${active}`);

export const getAllPropertiesAdmin = () => api.get<Property[]>('/properties/admin/all');

// ------ Favorites API ------
export const toggleFavorite = (propertyId: number) =>
  api.post<boolean>(`/favorites/${propertyId}`);

export const getFavoriteIds = () =>
  api.get<number[]>('/favorites');

export const isFavorited = (propertyId: number) =>
  api.get<boolean>(`/favorites/${propertyId}/status`);

// ✅ NEW: AI Voice Search API
export const voiceSearch = (query: string) =>
  api.post<VoiceSearchResponse>('/ai/voice-search', { query });

export interface AIHealthResponse {
  aiAvailable: boolean;
  status: string;
  message: string;
}

export const checkAIHealth = () =>
  api.get<AIHealthResponse>('/ai/health');

// ✅ NEW: Get location suggestions (autocomplete)
export const getLocationSuggestions = (query: string) =>
  api.get<string[]>(`/properties/locations/suggest?q=${query}`);