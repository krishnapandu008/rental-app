import { api } from './client';
import { Property } from '../types';

type BackendProperty = Omit<Property, 'location' | 'imageUrls'> & {
  location?: {
    name?: string;
    city?: string;
    displayName?: string;
    latitude?: number | string;
    longitude?: number | string;
  } | string;
  images?: { imageUrl?: string }[];
  imageUrls?: string[];
  latitude?: number | string;
  longitude?: number | string;
};

type PropertyPage = { content?: BackendProperty[] };

export interface PropertyQuery {
  location?: string;
  minPrice?: number;
  maxPrice?: number;
  bedrooms?: number;
  propertyType?: string;
  amenities?: string[];
  sortBy?: string;
  page?: number;
  size?: number;
}

export interface PropertyMutation {
  title: string;
  description?: string;
  locationId: number;
  propertyTypeId: number;
  rent: number;
  bedrooms: number;
  bathrooms?: number;
  squareFeet?: number;
  contactNumber: string;
  available?: boolean;
  visibility?: 'PUBLIC' | 'PRIVATE' | 'UNLISTED';
  amenityIds?: number[];
  latitude?: number;
  longitude?: number;
}

const toImageUrl = (imageUrl: string) => {
  const serverUrl = api.defaults.baseURL?.replace(/\/api\/?$/, '') || '';
  const localHostUrl = imageUrl.replace(/^https?:\/\/(?:localhost|127\.0\.0\.1)(?::\d+)?/i, serverUrl);
  if (localHostUrl !== imageUrl) return localHostUrl;
  if (/^https?:\/\//i.test(imageUrl)) return imageUrl;
  return `${serverUrl}${imageUrl.startsWith('/') ? imageUrl : `/${imageUrl}`}`;
};

const toCoordinate = (value: number | string | undefined) => {
  if (value === undefined || value === null || value === '') return undefined;
  const coordinate = typeof value === 'number' ? value : Number(value);
  return Number.isFinite(coordinate) ? coordinate : undefined;
};

const normalizeProperty = (property: BackendProperty): Property => {
  const location = typeof property.location === 'string' ? undefined : property.location;

  return {
    ...property,
    location: typeof property.location === 'string'
      ? property.location
      : location?.displayName || location?.name || location?.city || 'Unknown location',
    latitude: toCoordinate(property.latitude) ?? toCoordinate(location?.latitude),
    longitude: toCoordinate(property.longitude) ?? toCoordinate(location?.longitude),
    imageUrls: (property.imageUrls || property.images?.flatMap((image) =>
      image.imageUrl ? [toImageUrl(image.imageUrl)] : []
    ) || []).map(toImageUrl),
  };
};

export const getProperties = async (params?: PropertyQuery) => {
  const response = await api.get<PropertyPage | BackendProperty[]>('/properties', {
    params: {
      ...params,
      amenities: params?.amenities?.join(','),
    },
  });
  const properties = Array.isArray(response.data) ? response.data : response.data.content || [];
  return { ...response, data: properties.map(normalizeProperty) };
};

export const getPropertyById = async (id: number) => {
  const response = await api.get<BackendProperty>(`/properties/${id}`);
  return { ...response, data: normalizeProperty(response.data) };
};

export const getNearbyProperties = (lat: number, lng: number, radiusKm = 10) =>
  api.get<BackendProperty[]>('/properties/nearby', { params: { lat, lng, radiusKm } })
    .then((response) => ({ ...response, data: response.data.map(normalizeProperty) }));

export const getMapProperties = async () => {
  const response = await api.get<BackendProperty[] | PropertyPage>('/properties/map');
  const properties = Array.isArray(response.data) ? response.data : response.data.content || [];
  return { ...response, data: properties.map(normalizeProperty) };
};

export const getLocationSuggestions = (query: string) =>
  api.get<string[]>('/properties/locations/suggest', { params: { q: query } });

export const searchLocations = (query: string) =>
  api.get<string[]>('/properties/search-locations', { params: { q: query } });

export const getPropertyImages = async (propertyId: number) => {
  const response = await getPropertyById(propertyId);
  return { ...response, data: response.data.imageUrls || [] };
};

const createPropertyFormData = (data: PropertyMutation, images: Array<{ uri: string; name: string; type: string }> = []) => {
  const formData = new FormData();
  formData.append('dto', new Blob([JSON.stringify(data)], { type: 'application/json' }));
  images.forEach((image) => formData.append('images', image as unknown as Blob));
  return formData;
};

export const getOwnerProperties = (ownerId: number) =>
  api.get<BackendProperty[]>(`/properties/owner/${ownerId}`)
    .then((response) => ({ ...response, data: response.data.map(normalizeProperty) }));

export const createProperty = (data: PropertyMutation, images?: Array<{ uri: string; name: string; type: string }>) =>
  api.post<BackendProperty>('/properties', createPropertyFormData(data, images))
    .then((response) => ({ ...response, data: normalizeProperty(response.data) }));

export const updateProperty = (id: number, data: PropertyMutation) =>
  api.put<BackendProperty>(`/properties/${id}`, data)
    .then((response) => ({ ...response, data: normalizeProperty(response.data) }));

export const deleteProperty = (id: number) => api.delete(`/properties/${id}`);

export const uploadPropertyImages = (id: number, images: Array<{ uri: string; name: string; type: string }>) => {
  const formData = new FormData();
  images.forEach((image) => formData.append('images', image as unknown as Blob));
  return api.post<string[]>(`/properties/${id}/images`, formData);
};
