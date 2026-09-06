import { api } from './client';

// ✅ Refresh – no body needed; browser sends cookie automatically
export const refreshToken = () => api.post('/auth/refresh');

// ✅ Logout – no body needed; cookie will be cleared by server
export const logout = () => api.post('/auth/logout');