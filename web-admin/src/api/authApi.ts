// api/authApi.ts (new file)
import { api } from './client';

export const refreshToken = (refreshToken: string) =>
  api.post<{ accessToken: string; refreshToken: string; tokenType: string }>('/auth/refresh', { refreshToken });

export const logout = (refreshToken: string) =>
  api.post('/auth/logout', { refreshToken });