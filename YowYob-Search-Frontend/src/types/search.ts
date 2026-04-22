export interface SearchResult {
  id: string;
  name: string;
  description: string;
  price: number;
  type: 'product' | 'service' | 'shop';
  category: string;
  city: string;
  quartier?: string;
  rating?: number | null;
  reviewsCount?: number | null;
  detailsUrl?: string;
  // Nouveaux champs — tous optionnels
  phone?: string | null;
  openingHours?: string | null;
  imageUrl?: string | null;
  
  // UI specific fields that might be missing from backend
  images: string[];
  shop: {
    name: string;
    address: string;
    email?: string;
    phone?: string;
    description?: string;
  };
  location: {
    lat: number;
    lng: number;
  };
  latitude?: number;
  longitude?: number;
  tags: string[];
}