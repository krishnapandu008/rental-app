import axios from 'axios';
import { API_BASE_URL } from '../utils/constants';

export const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // ✅ sends HttpOnly cookies
});

// ✅ Only set Content-Type for non-FormData
api.interceptors.request.use((config) => {
  if (config.data instanceof FormData) {
    delete config.headers['Content-Type'];
  } else {
    config.headers['Content-Type'] = 'application/json';
  }
  return config;
});

// ✅ On 401, clear session and redirect to login
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Clear owner data (cookies are already cleared by backend on logout/expiry)
      localStorage.removeItem('owner');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;