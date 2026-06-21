import axios from 'axios';
import { API_BASE_URL } from '../utils/constants';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' }
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  console.log('Interceptor running, token:', token);
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
    console.log('Authorization header set:', config.headers['Authorization']);
  }
  console.log('Final headers:', config.headers);
  return config;
});

export default api;
