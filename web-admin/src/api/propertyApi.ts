import { api } from './client';
import { Property, PageResponse } from '../types';

export const getProperties = (params?: {
  location?: string;
  minPrice?: number;
  maxPrice?: number;
  bedrooms?: number;
  sortBy?: string;
  page?: number;
  size?: number;
}) =>
  api.get<PageResponse<Property>>('/properties', { params });

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
    Pick<Property, 'title' | 'description' | 'location' | 'rent' | 'bedrooms' | 'contactNumber' | 'available' | 'visibility' | 'latitude' | 'longitude'>
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