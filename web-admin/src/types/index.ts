export interface Property {
  id: number;
  title: string;
  description?: string;
  location: string;
  rent: number;
  bedrooms: number;
  contactNumber: string;
  available: boolean;
  ownerId: number;
  imageUrls: string[];
  visibility: 'PUBLIC' | 'PRIVATE' | 'UNLISTED';
  isActive: boolean;
  latitude?: number;
  longitude?: number;
  amenities?: string[];
  isFavorited?: boolean;
}

export interface Owner {
  id: number;
  email: string;
  name: string;
  phone: string;
  token: string;
  refreshToken: string;
  role: string;
  isActive?: boolean;
  isLocked?: boolean;
  createdAt?: string;
  updatedAt?: string;
  lastLoginAt?: string;
  avatarUrl?: string;
}

export interface AuditLog {
  id: number;
  adminId: number;
  adminEmail: string;
  action: string;
  details: string;
  ipAddress: string;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// ✅ NEW: AI Voice Search Types
export interface SearchFilters {
  location?: string | null;
  minRent?: number | null;
  maxRent?: number | null;
  bedrooms?: number | null;
  amenities?: string[] | null;
  explanation?: string | null;
}

export interface VoiceSearchResponse {
  transcript: string;
  explanation: string;
  filters: SearchFilters;
  properties: Property[];
  totalResults: number;
  aiAvailable: boolean;
}