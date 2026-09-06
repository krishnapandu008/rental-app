// src/config/index.ts

declare const process: {
	env?: {
		EXPO_PUBLIC_API_URL?: string;
	};
};

// Set EXPO_PUBLIC_API_URL for a physical device or emulator when the host differs.
const DEV_API_URL = process.env?.EXPO_PUBLIC_API_URL?.replace(/\/$/, '')
	|| 'http://192.168.31.151:8585/api';

// For Android Studio Emulator testing (Uncomment if using emulator instead of physical phone)
// const DEV_API_URL = 'http://10.0.2.2:8585/api'; 

// Production Environment: Hetzner cloud URL
const PROD_API_URL = 'https://ksdcnit.com/api';

// 🌟 Safe Check: Check if __DEV__ exists in the global scope safely without throwing errors
const isDevelopment = typeof __DEV__ !== 'undefined' ? __DEV__ : false;

export const API_BASE_URL = isDevelopment ? DEV_API_URL : PROD_API_URL;