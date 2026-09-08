import { api } from './client';

export interface LoginResponse {
  id: number;
  email: string;
  name: string;
  phone: string;
  role: string;
  isActive?: boolean;
  isLocked?: boolean;
  token?: string;
  refreshToken?: string;
}

export type OwnerCredentials = {
  email: string;
  password: string;
};

export type OwnerRegistration = OwnerCredentials & {
  name: string;
  phone: string;
};

export const loginOwner = (credentials: OwnerCredentials) =>
  api.post<LoginResponse>('/owners/login', credentials);

export const registerOwner = (data: OwnerRegistration) =>
  api.post<LoginResponse>('/owners/register', data);
