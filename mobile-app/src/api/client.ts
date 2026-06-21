import axios from 'axios';
import { API_BASE_URL } from '../config';   // import from config

// Use this for local testing with physical device (replace with your PC's IP)
// const API_BASE_URL = 'http://192.168.31.151:8585/api'; // local IP
// For Android emulator:
// const API_BASE_URL = 'http://10.0.2.2:8585/api';
// For production (Hetzner):
// const API_BASE_URL = 'https://ksdcnit.com/api';

export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});