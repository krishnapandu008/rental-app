// src/config/index.ts

declare const process: {
	env?: {
		EXPO_PUBLIC_API_URL?: string;
	};
};

// Set EXPO_PUBLIC_API_URL=http://YOUR_COMPUTER_IP:8585/api for local backend testing.
const OVERRIDE_API_URL = process.env?.EXPO_PUBLIC_API_URL?.replace(/\/$/, '');

// Production Environment: Hetzner cloud URL
const PROD_API_URL = 'https://ksdcnit.com/api';

export const API_BASE_URL = OVERRIDE_API_URL || PROD_API_URL;