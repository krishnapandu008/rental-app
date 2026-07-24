import { api } from './client';
import { Owner, AuditLog } from '../types';

// ---------- Users ----------
export const getAllUsers = () => api.get<Owner[]>('/admin/users');
export const getUserDetails = (userId: number) => api.get<Owner>(`/admin/users/${userId}`);

export const createUser = (data: {
  email: string;
  password: string;
  name: string;
  phone: string;
  role?: string;
}) => api.post<Owner>('/admin/users', data);

export const updateUser = (userId: number, data: {
  email: string;
  name: string;
  phone: string;
}) => api.put<Owner>(`/admin/users/${userId}`, data);

export const updateUserRole = (userId: number, role: string) =>
  api.put<Owner>(`/admin/users/${userId}/role`, { role });

export const toggleUserActive = (userId: number) =>
  api.patch<Owner>(`/admin/users/${userId}/toggle-active`);

export const deleteUser = (userId: number) => api.delete(`/admin/users/${userId}`);

// ---------- Audit Logs ----------
export const getAuditLogs = () => api.get<AuditLog[]>('/admin/audit-logs');