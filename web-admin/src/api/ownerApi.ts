import { api } from './client';
import { Owner } from '../types';

export const registerOwner = (data: { email: string; password: string; name: string; phone: string }) =>
  api.post<Owner>('/owners/register', data);

export const loginOwner = (credentials: { email: string; password: string }) =>
  api.post<Owner>('/owners/login', credentials);