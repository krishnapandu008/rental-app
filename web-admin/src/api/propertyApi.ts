import { api } from './client';
import { Property } from '../types';

export const getProperties = () => api.get<Property[]>('/properties');
export const getMyProperties = (ownerId: number) => api.get<Property[]>(`/properties/owner/${ownerId}`);

export const addProperty = (
  data: Omit<Property, 'id' | 'available' | 'imageUrls'>,
  images?: File[]
) => {
  const formData = new FormData();
  formData.append('property', new Blob([JSON.stringify(data)], { type: 'application/json' }));
  images?.forEach((file) => formData.append('images', file));
  return api.post<Property>('/properties', formData);
};

export const updateProperty = (id: number, data: Partial<Property>) =>
  api.put<Property>(`/properties/${id}`, data); // this one IS plain JSON on the backend

export const uploadImages = (id: number, images: File[]) => {
  const formData = new FormData();
  images.forEach((file) => formData.append('images', file));
  return api.post<string[]>(`/properties/${id}/images`, formData);
};

export const deleteProperty = (id: number) => api.delete(`/properties/${id}`);

// Admin-only
export const getAllPropertiesAdmin = () => api.get<Property[]>('/properties/admin/all');