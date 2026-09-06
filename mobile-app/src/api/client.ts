// src/api/client.ts
import axios from 'axios';
import { API_BASE_URL } from '../utils/constants';

export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
});

// 🔍 REQUEST LOGGER: Tracks exactly what leaves the app
api.interceptors.request.use(
  (config) => {
    if (typeof FormData !== 'undefined' && config.data instanceof FormData) {
      delete config.headers['Content-Type'];
    }
    console.log('=== 🚀 OUTGOING REQUEST ===');
    console.log(`URL: ${config.baseURL}${config.url}`);
    console.log('Headers:', JSON.stringify(config.headers, null, 2));
    return config;
  },
  (error) => {
    console.error('❌ Request Setup Error:', error);
    return Promise.reject(error);
  }
);

// 🔍 RESPONSE LOGGER: Tracks exactly why the connection drops
api.interceptors.response.use(
  (response) => {
    console.log('=== ✅ RESPONSE RECEIVED ===');
    console.log('Status:', response.status);
    return response;
  },
  (error) => {
    console.log('=== ❌ NETWORK BREAKDOWN DETAILS ===');
    if (error.response) {
      // The server responded with a status code outside the 2xx range
      console.log('Server Error Data:', error.response.data);
      console.log('Server Error Status:', error.response.status);
    } else if (error.request) {
      // The request was made but no response was received
      console.log('No response received from server.');
      //  Replace with this type-safe line:
console.log('Native Request Instance Object Keys:', error.request ? Object.keys(error.request) : 'No request object');
      console.log('Request Config Details:', {
        timeout: error.config?.timeout,
        url: error.config?.url,
        baseURL: error.config?.baseURL
      });
    } else {
      // Something happened in setting up the request that triggered an Error
      console.log('Error Message:', error.message);
    }
    return Promise.reject(error);
  }
);