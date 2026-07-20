import { api } from './client';
import { Owner } from '../types';

export const getAllUsers = () => api.get<Owner[]>('/admin/users');
export const updateUserRole = (userId: number, role: string) =>
    api.put<Owner>(`/admin/users/${userId}/role`, { role });
export const deleteUser = (userId: number) => api.delete(`/admin/users/${userId}`);