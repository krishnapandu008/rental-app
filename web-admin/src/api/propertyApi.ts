import { api } from './client';
import { Property } from '../types';

export const getProperties = () => api.get<Property[]>('/properties');
export const getMyProperties = (ownerId: number) => api.get<Property[]>(`/properties/owner/${ownerId}`);
export const addProperty = (data: Omit<Property, 'id' | 'available'> & { ownerId: number }) =>
  api.post<Property>('/properties', data);
export const deleteProperty = (id: number) => api.delete(`/properties/${id}`);