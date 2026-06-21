// Use the environment variable from .env (which is '/api' in development)
// For production, set VITE_API_BASE_URL in .env.production to your actual domain.
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';