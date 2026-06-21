import { api } from './client';
import { Property } from '../types';

export const getProperties = () => api.get<Property[]>('/properties');
export const getPropertyById = (id: number) => api.get<Property>(`/properties/${id}`); 
