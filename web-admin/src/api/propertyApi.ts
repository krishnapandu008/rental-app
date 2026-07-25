import { api } from './client';
import { Property } from '../types';

// ✅ Updated to accept optional query parameters (location search)
export const getProperties = (params?: { location?: string }) =>
  api.get<Property[]>('/properties', { params });

export const getMyProperties = (ownerId: number) =>
  api.get<Property[]>(`/properties/owner/${ownerId}`);

// Create – always use FormData (even if no images)
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
  };
  formData.append('dto', new Blob([JSON.stringify(payload)], { type: 'application/json' }));

  if (images && images.length > 0) {
    images.forEach((file) => formData.append('images', file));
  }

  // Return the POST with a custom header to force multipart
  return api.post<Property>('/properties', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

export const updateProperty = (
  id: number,
  data: Partial<
    Pick<Property, 'title' | 'description' | 'location' | 'rent' | 'bedrooms' | 'contactNumber' | 'available' | 'visibility'>
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

// Admin-only endpoints
export const togglePropertyActive = (id: number, active: boolean) =>
  api.patch(`/properties/admin/${id}/active?active=${active}`);

export const getAllPropertiesAdmin = () => api.get<Property[]>('/properties/admin/all');