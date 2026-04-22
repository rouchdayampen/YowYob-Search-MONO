import React from "react";
import { Badge } from '@/components/ui/badge';
import { SearchResult } from '@/types/search';
import { Star, Phone, Clock } from "lucide-react";

interface Props {
  item: SearchResult;
  onClick?: (item: SearchResult) => void;
}

// Composant étoiles — ne s'affiche que si rating est réel
function StarRating({ rating, count }: { rating: number; count: number }) {
  return (
    <span className="flex items-center gap-1 text-sm">
      <span className="font-semibold text-gray-800 dark:text-gray-200">{rating.toFixed(1)}</span>
      {Array.from({ length: 5 }).map((_, i) => (
        <Star
          key={i}
          size={14}
          className={i < Math.round(rating) ? "text-yellow-400 fill-yellow-400" : "text-gray-300 dark:text-gray-600"}
        />
      ))}
      <span className="text-gray-500 dark:text-gray-400">({count})</span>
    </span>
  );
}

export const ResultListItem: React.FC<Props> = ({ item: result, onClick }) => {
  const displayImage = result.imageUrl || (result.images && result.images.length > 0 ? result.images[0] : null);

  return (
    <div 
        className="flex gap-4 p-4 border-b border-gray-200 dark:border-gray-800 hover:bg-gray-50 dark:hover:bg-gray-800/50 transition cursor-pointer max-w-2xl"
        onClick={() => onClick?.(result)}
    >

      {/* Contenu principal */}
      <div className="flex-1 min-w-0">

        {/* Titre */}
        <h3 className="text-xl font-normal text-[#1a0dab] dark:text-[#8ab4f8] group-hover:underline cursor-pointer truncate leading-snug mb-1">
          {result.title || result.name}
        </h3>

        {/* Rating — masqué si null, jamais de valeur inventée */}
        <div className="flex items-center gap-2 mb-1 text-sm text-gray-500">
            {result.rating != null && result.reviewsCount != null ? (
            <StarRating rating={result.rating} count={result.reviewsCount} />
            ) : (
            <span className="text-xs text-gray-400">Pas encore d'avis</span>
            )}
            {result.category && (
                <span>• {result.category}</span>
            )}
        </div>

        {/* Ville + Téléphone */}
        <div className="flex items-center gap-2 mb-1 text-sm text-gray-500 dark:text-gray-400">
          {(result.city || result.quartier || (result.shop && result.shop.address)) && (
            <span className="truncate">
                {[result.quartier, result.city].filter(Boolean).join(', ') || result.shop.address}
            </span>
          )}
          {result.phone ? (
            <span className="flex items-center gap-1">
              • {result.phone}
            </span>
          ) : (
            <span className="text-gray-400 text-xs">• Tél. non renseigné</span>
          )}
        </div>

        {/* Horaires */}
        <div className="flex items-center gap-1 mb-2 text-sm text-gray-500 dark:text-gray-400">
          {result.openingHours ? (
            <span>{result.openingHours}</span>
          ) : (
            <span className="text-gray-400 text-xs">Horaires non renseignés</span>
          )}
        </div>
        
        {/* Description snippet */}
        <p className="text-sm text-gray-600 dark:text-gray-300 leading-relaxed line-clamp-2 mt-1 flex gap-2">
            {result.description && (
                <span>
                   <span className="text-[#1a0dab] dark:text-[#8ab4f8] mr-1">👤</span> 
                   "{result.description}"
                </span>
            )}
        </p>

      </div>

      {/* Image — masquée si absente */}
      {displayImage && (
        <div className="flex-shrink-0 w-24 h-24 mt-1 rounded-lg overflow-hidden bg-gray-100 border border-gray-200 dark:border-gray-700">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img
            src={displayImage}
            alt={result.title || result.name}
            className="w-full h-full object-cover"
            onError={(e) => { (e.target as HTMLImageElement).style.display = "none"; }}
          />
        </div>
      )}

    </div>
  );
}
