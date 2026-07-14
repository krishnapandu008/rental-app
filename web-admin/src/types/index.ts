export interface Property {
  id: number;
  title: string;
  description?: string;
  location: string;
  rent: number;
  bedrooms: number;
  contactNumber: string;
  available: boolean;
  ownerId: number;        // ← backend added this
  imageUrls: string[];    // ← backend returns this, currently invisible to you
}

export interface Owner {
  id: number;
  email: string;
  name: string;
  phone: string;
  token: string;
  refreshToken: string;   // ← new, required for refresh flow
  role: string;           // ← new, "OWNER" | "ADMIN" — you'll need this for admin UI
}