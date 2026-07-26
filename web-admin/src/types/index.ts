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
  latitude?: number;   // ✅ NEW
  longitude?: number;  // ✅ NEW
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