import { api } from './client';
import { Property } from '../types';

export const getProperties = () => api.get<Property[]>('/properties');
export const getPropertyById = (id: number) => api.get<Property>(`/properties/${id}`); 

// Get property images - returns array of image URLs
export const getPropertyImages = (propertyId: number) => 
  api.get<string[]>(`/properties/${propertyId}/images`).catch(() => ({ data: [] })); 
