import { api } from './client';

export interface LoginResponse {
  id: number;
  email: string;
  name: string;
  phone: string;
  token: string;
}

export const loginOwner = (credentials: { email: string; password: string }) =>
  api.post<LoginResponse>('/owners/login', credentials);

export const registerOwner = (data: { email: string; password: string; name: string; phone: string }) =>
  api.post<LoginResponse>('/owners/register', data);
