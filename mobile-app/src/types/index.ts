export interface Property {
  id: number;
  title: string;
  description?: string;
  location: string;
  locationId?: number;
  propertyType?: { id?: number; typeName?: string; icon?: string } | string;
  propertyTypeId?: number;
  owner?: { id?: number; name?: string; email?: string; phone?: string };
  rent: number;
  bedrooms: number;
  bathrooms?: number;
  squareFeet?: number;
  contactNumber: string;
  available: boolean;
  visibility?: 'PUBLIC' | 'PRIVATE' | 'UNLISTED' | string;
  isActive?: boolean;
  latitude?: number;
  longitude?: number;
  imageUrls?: string[];
  images?: { imageUrl?: string; isPrimary?: boolean; caption?: string }[];
  amenities?: ({ amenityName?: string; icon?: string } | string)[];
  distance?: number;
  favorited?: boolean;
  createdAt?: string;
  updatedAt?: string;
}  
