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
  const safeParams: Record<string, string | number | string[]> = {};
  if (params?.location) safeParams.location = params.location;
  if (params?.minPrice !== undefined) safeParams.minPrice = params.minPrice;
  if (params?.maxPrice !== undefined) safeParams.maxPrice = params.maxPrice;
  if (params?.bedrooms !== undefined) safeParams.bedrooms = params.bedrooms;
  if (params?.propertyType) safeParams.propertyType = params.propertyType;
  if (params?.sortBy) safeParams.sortBy = params.sortBy;
  if (params?.page !== undefined) safeParams.page = params.page;
  if (params?.size !== undefined) safeParams.size = params.size;
  if (params?.amenities?.length) {
    safeParams.amenities = SEND_AMENITIES_AS_CSV
      ? params.amenities.join(',')
      : params.amenities;
  }

  // Debug log (remove in production)
  console.log('🔍 Sending params to /properties:', safeParams);

  return api.get<PageResponse<Property>>('/properties', { params: safeParams });
};

export const getMyProperties = (ownerId: number) =>
  api.get<Property[]>(`/properties/owner/${ownerId}`);

export const addProperty = (
  data: {
    title: string;
    description?: string;
    location: string;
    rent: number;
    bedrooms: number;
    contactNumber: string;
    visibility: Property['visibility'];
    latitude?: number;
    longitude?: number;
    amenities?: string[];
    locationId?: number;
    propertyTypeId?: number;
    bathrooms?: number;
    squareFeet?: number;
    amenityIds?: number[];
    available?: boolean;
  },
  images?: File[]
) => {
  const formData = new FormData();

  const payload = {
    title: data.title,
    description: data.description,
    location: data.location,
    locationId: data.locationId,
    propertyTypeId: data.propertyTypeId,
    rent: data.rent,
    bedrooms: data.bedrooms,
    bathrooms: data.bathrooms || 0,
    squareFeet: data.squareFeet || 0,
    contactNumber: data.contactNumber,
    available: data.available ?? true,
    visibility: data.visibility || 'PUBLIC',
    latitude: data.latitude,
    longitude: data.longitude,
    amenityIds: data.amenityIds || [],
  };
  formData.append('dto', new Blob([JSON.stringify(payload)], { type: 'application/json' }));

  if (images && images.length > 0) {
    images.forEach((file) => formData.append('images', file));
  }

  return api.post<Property>('/properties', formData);
};

export const updateProperty = (
  id: number,
  data: Partial<
    Pick<Property, 'title' | 'description' | 'rent' | 'bedrooms' | 'contactNumber' | 'available' | 'visibility' | 'latitude' | 'longitude'> & {
      locationId?: number;
      propertyTypeId?: number;
      bathrooms?: number;
      squareFeet?: number;
      amenityIds?: number[];
    }
  >
) => {
  const payload = {
    title: data.title,
    description: data.description,
    locationId: data.locationId,
    propertyTypeId: data.propertyTypeId,
    rent: data.rent,
    bedrooms: data.bedrooms,
    bathrooms: data.bathrooms,
    squareFeet: data.squareFeet,
    contactNumber: data.contactNumber,
    available: data.available,
    visibility: data.visibility,
    latitude: data.latitude,
    longitude: data.longitude,
    amenityIds: data.amenityIds,
  };
  return api.put<Property>(`/properties/${id}`, payload);
};

export const uploadImages = (id: number, images: File[]) => {
  const formData = new FormData();
  images.forEach((file) => formData.append('images', file));
  return api.post<string[]>(`/properties/${id}/images`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

export const deleteProperty = (id: number) => api.delete(`/properties/${id}`);

export const togglePropertyActive = (id: number, active: boolean) =>
  api.patch(`/properties/admin/${id}/active?active=${active}`);

// Favorites
export const toggleFavorite = (propertyId: number) =>
  api.post<boolean>(`/favorites/${propertyId}`);

export const getFavoriteIds = () =>
  api.get<number[]>('/favorites');

export const isFavorited = (propertyId: number) =>
  api.get<boolean>(`/favorites/${propertyId}/status`);

// AI Voice Search
export const voiceSearch = (query: string) =>
  api.post<VoiceSearchResponse>('/ai/voice-search', { query });

export interface AIHealthResponse {
  aiAvailable: boolean;
  status: string;
  message: string;
}

export const checkAIHealth = () =>
  api.get<AIHealthResponse>('/ai/health');

// Location suggestions
export const getLocationSuggestions = (query: string) =>
  api.get<string[]>(`/properties/locations/suggest?q=${query}`);

export const searchLocations = (query: string) =>
  api.get<string[]>(`/properties/search-locations?q=${encodeURIComponent(query)}`);