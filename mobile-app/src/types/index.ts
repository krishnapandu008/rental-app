export interface Property {
  id: number;
  title: string;
  description?: string;
  location: string;
  rent: number;
  bedrooms: number;
  contactNumber: string;
  available: boolean;
  latitude?: number;
  longitude?: number;
  imageUrls?: string[];
}  
