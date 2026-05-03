import { httpClient } from './http-client';
import { API_ENDPOINTS } from '../constants/api-endpoints';

export interface GeoLocation {
    lat: number;
    lng: number;
    city?: string;
    country?: string;
}

export interface RouteInfo {
    distance: number; // in meters
    duration: number; // in seconds
    polyline: string; // encoded polyline or simple list of points
}

class GeoService {
    /**
     * Get user location based on IP address (inferred by backend)
     */
    async getIpLocation(): Promise<GeoLocation | null> {
        try {
            // Get public IP to avoid localhost (127.0.0.1) resolution issues locally
            let userIp = '';
            const ipProviders = [
                'https://api.ipify.org?format=json',
                'https://api64.ipify.org?format=json',
                'https://freeipapi.com/api/json',
                'https://jsonip.com/'
            ];

            for (const provider of ipProviders) {
                try {
                    const ipResponse = await fetch(provider);
                    if (ipResponse.ok) {
                        const ipData = await ipResponse.json();
                        // ipify returns {ip: "..."}, jsonip returns {ip: "..."}, freeipapi returns {ipAddress: "..."}
                        userIp = ipData.ip || ipData.ipAddress;
                        if (userIp) break;
                    }
                } catch (e) {
                    continue; // try next provider
                }
            }

            // 1. Direct Client-Side Fallback for IP Geolocation (bypasses Docker network issues entirely)
            if (userIp) {
                try {
                    const geoResponse = await fetch(`https://ipapi.co/${userIp}/json/`);
                    if (geoResponse.ok) {
                        const geoData = await geoResponse.json();
                        if (geoData.latitude && geoData.longitude) {
                            return {
                                lat: parseFloat(geoData.latitude),
                                lng: parseFloat(geoData.longitude),
                                city: geoData.city,
                                country: geoData.country_name
                            };
                        }
                    }
                } catch (fallbackError) {
                    console.warn('Frontend ipapi fallback failed:', fallbackError);
                }
            } else {
                console.warn('Could not fetch public IP from any provider, relying on backend headers inferred IP');
            }

            // 2. Try Backend as a last resort
            const endpoint = userIp 
                ? `${API_ENDPOINTS.GEO_DISTANCE.replace('/distance', '/ip-location')}?ip=${encodeURIComponent(userIp)}`
                : `${API_ENDPOINTS.GEO_DISTANCE.replace('/distance', '/ip-location')}`;
                
            const response = await httpClient.get<any>(endpoint);
            
            if (response && response.latitude && response.longitude) {
                return {
                    lat: response.latitude,
                    lng: response.longitude,
                    city: response.city,
                    country: response.country
                };
            }
            return null;
        } catch (error) {
            console.error('Failed to get IP location:', error);
            return null;
        }
    }

    /**
     * Geocode an address to get its coordinates
     */
    async geocode(address: string): Promise<GeoLocation | null> {
        try {
            // First try backend API
            const response = await httpClient.get<any>(`${API_ENDPOINTS.GEO_GEOCODE}?address=${encodeURIComponent(address)}`);
            if (response && response.latitude && response.longitude) {
                return {
                    lat: response.latitude,
                    lng: response.longitude,
                    city: response.address // The backend might return address string in address field
                };
            }
            return null;
        } catch (error) {
            console.warn('Backend geocode failed, falling back to client-side Nominatim:', error);
            // Fallback to client-side Nominatim if backend Docker container has network issues
            try {
                const nominatimUrl = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}&limit=1`;
                const nomResponse = await fetch(nominatimUrl, {
                    headers: { 'User-Agent': 'YowYob-Frontend-Client' }
                });
                if (nomResponse.ok) {
                    const data = await nomResponse.json();
                    if (data && data.length > 0) {
                        return {
                            lat: parseFloat(data[0].lat),
                            lng: parseFloat(data[0].lon),
                            city: data[0].display_name
                        };
                    }
                }
            } catch (fallbackError) {
                console.error('Failed to geocode address (both backend and fallback failed):', fallbackError);
            }
            return null;
        }
    }

    /**
     * Get browser geolocation
     */
    getCurrentPosition(): Promise<GeoLocation> {
        return new Promise((resolve, reject) => {
            if (!navigator.geolocation) {
                reject(new Error('Geolocation is not supported by your browser'));
                return;
            }

            navigator.geolocation.getCurrentPosition(
                (position) => {
                    resolve({
                        lat: position.coords.latitude,
                        lng: position.coords.longitude
                    });
                },
                (error) => {
                    reject(error);
                },
                { enableHighAccuracy: true, timeout: 5000, maximumAge: 0 }
            );
        });
    }

    /**
     * Calculate distance between two points
     */
    async getDistance(loc1: GeoLocation, loc2: GeoLocation): Promise<number | null> {
        try {
            const response = await httpClient.post<any>(API_ENDPOINTS.GEO_DISTANCE, {
                lat1: loc1.lat,
                lon1: loc1.lng,
                lat2: loc2.lat,
                lon2: loc2.lng
            });
            // Backend returns DistanceResponse with distanceKm and distanceMiles
            return response.distanceKm ? response.distanceKm * 1000 : null; // Return in meters
        } catch (error) {
            console.error('Failed to calculate distance:', error);
            return null;
        }
    }

    /**
     * Get route between two points
     */
    /**
     * Calculate Haversine distance in meters
     */
    private calculateHaversineDistance(loc1: GeoLocation, loc2: GeoLocation): number {
        const R = 6371e3; // metres
        const φ1 = loc1.lat * Math.PI / 180;
        const φ2 = loc2.lat * Math.PI / 180;
        const Δφ = (loc2.lat - loc1.lat) * Math.PI / 180;
        const Δλ = (loc2.lng - loc1.lng) * Math.PI / 180;

        const a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
            Math.cos(φ1) * Math.cos(φ2) *
            Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Get route between two points
     */
    async getRoute(start: GeoLocation, end: GeoLocation, mode: 'driving' | 'walking' | 'cycling' = 'driving'): Promise<RouteInfo | null> {
        try {
            const response = await httpClient.get<any>(`${API_ENDPOINTS.GEO_ROUTE}?startLat=${start.lat}&startLon=${start.lng}&endLat=${end.lat}&endLon=${end.lng}&mode=${mode}`);
            // Ensure we have valid numbers
            if (response && typeof response.distance === 'number') {
                let finalDuration = response.duration || this.estimateDuration(response.distance, mode);
                // Override api duration for non-driving modes in case the backend doesn't support them properly yet
                if (mode !== 'driving') {
                    finalDuration = this.estimateDuration(response.distance, mode);
                }

                return {
                    distance: response.distance,
                    duration: finalDuration,
                    polyline: response.polyline
                };
            }
            throw new Error('Invalid response from route service');
        } catch (error) {
            console.debug('Route API unavailable, using Haversine fallback.');
            // Fallback: Calculate direct distance client-side
            const directDistance = this.calculateHaversineDistance(start, end);

            return {
                distance: directDistance,
                duration: this.estimateDuration(directDistance, mode),
                polyline: JSON.stringify([
                    [start.lat, start.lng],
                    [end.lat, end.lng]
                ])
            };
        }
    }

    private estimateDuration(distance: number, mode: 'driving' | 'walking' | 'cycling'): number {
        // Speeds in m/s
        let speed = 13.8; // Driving: ~50 km/h
        if (mode === 'walking') speed = 1.4; // Walking: ~5 km/h
        if (mode === 'cycling') speed = 5.5; // Cycling: ~20 km/h
        return distance / speed;
    }
}

export const geoService = new GeoService();

export const getUserLocation = async (): Promise<GeoLocation | null> => {

  // Tentative 1 : localisation par IP (silencieuse, pas de pop-up)
  try {
    const ipLocation = await geoService.getIpLocation();
    if (ipLocation) return ipLocation;
  } catch {
    // IP échouée → on tente le GPS silencieusement
  }

  // Tentative 2 : GPS navigateur en fallback silencieux
  const gpsSupported = typeof window !== 'undefined'
    && 'geolocation' in navigator;

  if (gpsSupported) {
    try {
      const position = await new Promise<GeolocationPosition>(
        (resolve, reject) => {
          navigator.geolocation.getCurrentPosition(resolve, reject, {
            timeout: 5000,
            maximumAge: 60000,
          });
        }
      );
      return {
        lat: position.coords.latitude,
        lng: position.coords.longitude,
      };
    } catch {
      // GPS aussi échoué — on abandonne proprement
    }
  }

  return null;
};

