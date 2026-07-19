import axios from 'axios';
import { API_BASE_URL } from '../utils/constants';

export const api = axios.create({ baseURL: API_BASE_URL });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers['Authorization'] = `Bearer ${token}`;

  // CRITICAL: Do NOT set Content-Type for FormData
  // Let Axios handle it automatically.
  if (config.data instanceof FormData) {
    delete config.headers['Content-Type'];
  } else {
    config.headers['Content-Type'] = 'application/json';
  }

  return config;
});

let isRefreshing = false;
let pendingQueue: Array<() => void> = [];

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      const refreshTokenValue = localStorage.getItem('refreshToken');

      if (!refreshTokenValue) {
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(error);
      }

      if (!isRefreshing) {
        isRefreshing = true;
        try {
          const res = await axios.post(`${API_BASE_URL}/auth/refresh`, { refreshToken: refreshTokenValue });
          localStorage.setItem('token', res.data.accessToken);
          localStorage.setItem('refreshToken', res.data.refreshToken);
          pendingQueue.forEach((cb) => cb());
          pendingQueue = [];
        } catch (refreshError) {
          localStorage.clear();
          window.location.href = '/login';
          return Promise.reject(refreshError);
        } finally {
          isRefreshing = false;
        }
      }

      return new Promise((resolve) => {
        pendingQueue.push(() => resolve(api(originalRequest)));
      });
    }
    return Promise.reject(error);
  }
);

export default api;