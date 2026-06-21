export interface Property {
  id: number;
  title: string;
  description?: string;
  location: string;
  rent: number;
  bedrooms: number;
  contactNumber: string;
  available: boolean;
}

export interface Owner {
  id: number;
  email: string;
  name: string;
  phone: string;
  token: string;          // ← add this line
}