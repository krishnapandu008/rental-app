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
  visibility: 'PUBLIC' | 'PRIVATE' | 'UNLISTED'; // NEW
  isActive: boolean; // NEW
}

export interface Owner {
  id: number;
  email: string;
  name: string;
  phone: string;
  token: string;
  refreshToken: string;
  role: string; // "OWNER" | "ADMIN"
}