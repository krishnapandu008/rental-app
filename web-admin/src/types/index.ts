// ================================================================
// LOCATION
// ================================================================
export interface Location {
  id: number;
  displayName: string;
  district?: string;
  state?: string;
  country?: string;
  pinCode?: string;
  latitude?: number;
  longitude?: number;
}

// ================================================================
// PROPERTY TYPE
// ================================================================
export interface PropertyType {
  id: number;
  typeName: string;
  icon?: string;
}

// ================================================================
// OWNER
// ================================================================
export interface Owner {
  id: number;
  email: string;
  name: string;
  phone?: string;        // Made optional to match backend
  token?: string;          // optional
  refreshToken?: string;   // optional
  role: string;
  isActive?: boolean;
  isLocked?: boolean;
  createdAt?: string;
  updatedAt?: string;
  lastLoginAt?: string;
  avatarUrl?: string;
}

// ================================================================
// AMENITY
// ================================================================
export interface Amenity {
  id: number;
  amenityName: string;
  icon?: string;
  category?: string;
}

// ================================================================
// IMAGE
// ================================================================
export interface Image {
  id: number;
  imageUrl: string;
  isPrimary: boolean;
  displayOrder?: number;
  caption?: string;
}

// ================================================================
// PROPERTY
// ================================================================
export interface Property {
  id: number;
  title: string;
  description?: string;
  location: Location;          // object, not string
  propertyType: PropertyType;   // object, not string
  owner: Owner;                // object, not string
  rent: number;
  bedrooms: number;
  bathrooms?: number;
  squareFeet?: number;
  contactNumber: string;
  available: boolean;
  visibility: 'PUBLIC' | 'PRIVATE' | 'UNLISTED';
  isActive: boolean;
  latitude?: number;
  longitude?: number;
  amenities: Amenity[];        // array of objects
  images: Image[];             // array of objects
  distance?: number;
  createdAt?: string;
  updatedAt?: string;
  favorited?: boolean;         // renamed from isFavorited for clarity
}

// ================================================================
// PAGE RESPONSE (pagination)
// ================================================================
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// ================================================================
// AUDIT LOG
// ================================================================
export interface AuditLog {
  id: number;
  adminId: number;
  adminEmail: string;
  action: string;
  details: string;
  ipAddress: string;
  timestamp: string;
}

// ================================================================
// VOICE SEARCH
// ================================================================
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