import React from "react";
import { SearchResult } from '@/types/search';
import { Star, User } from "lucide-react";

interface Props {
  item: SearchResult;
  onClick?: (item: SearchResult) => void;
}

// Composant étoiles — style Google
function StarRating({ rating, count }: { rating: number; count: number }) {
  return (
    <div className="flex items-center gap-1 text-[13px]">
      <span className="font-medium text-[#70757a]">{rating.toFixed(1)}</span>
      <div className="flex items-center">
        {Array.from({ length: 5 }).map((_, i) => (
          <Star
            key={i}
            size={14}
            className={i < Math.floor(rating) 
                ? "text-[#fbbc04] fill-[#fbbc04]" 
                : (i < rating ? "text-[#fbbc04] fill-[#fbbc04] opacity-50" : "text-[#dadce0] fill-[#dadce0]")}
          />
        ))}
      </div>
      <span className="text-[#70757a]">({count})</span>
    </div>
  );
}

export const ResultListItem: React.FC<Props> = ({ item: result, onClick }) => {
  const displayImage = result.imageUrl || (result.images && result.images.length > 0 ? result.images[0] : null);

  // Déterminer le statut (Ouvert/Fermé) — Simulation si absent
  const isOpen = result.openingHours?.toLowerCase().includes("fermé") ? false : true;
  const statusColor = isOpen ? "text-[#188038]" : "text-[#d93025]";
  const statusText = result.openingHours || (isOpen ? "Ouvert" : "Fermé");

  return (
    <div 
        className="flex gap-4 py-4 px-0 border-b border-[#dadce0] dark:border-gray-800 hover:bg-gray-50/50 dark:hover:bg-gray-800/30 transition-colors cursor-pointer w-full group"
        onClick={() => onClick?.(result)}
    >
      <div className="flex-1 min-w-0">
        {/* Titre — Style Google Blue */}
        <h3 className="text-[18px] font-normal text-[#1a0dab] dark:text-[#8ab4f8] group-hover:underline leading-tight mb-0.5">
          {result.title || result.name}
        </h3>

        {/* Ligne 1: Rating et Catégorie */}
        <div className="flex items-center gap-1.5 flex-wrap">
            {result.rating != null ? (
                <StarRating rating={result.rating} count={result.reviewsCount || 0} />
            ) : null}
            {result.category && (
                <span className="text-[13px] text-[#70757a] dark:text-gray-400">
                   {result.rating != null ? " · " : ""}{result.category}
                </span>
            )}
        </div>

        {/* Ligne 2: Ville et Téléphone */}
        <div className="text-[13px] text-[#70757a] dark:text-gray-400 mt-0.5">
          <span>{result.city || result.quartier || "Douala"}</span>
          {result.phone && (
            <span> · {result.phone}</span>
          )}
        </div>

        {/* Ligne 3: Statut Horaires */}
        <div className="text-[13px] mt-0.5">
          <span className={`${statusColor} font-medium`}>{isOpen ? "Ouvert" : "Fermé"}</span>
          {result.openingHours && (
            <span className="text-[#70757a] dark:text-gray-400"> · {result.openingHours.replace(/Fermé|Ouvert/gi, "").trim()}</span>
          )}
        </div>

        {/* Ligne 4: Snippet / Livraison / Commentaire */}
        <div className="mt-1 flex items-start gap-2">
           {result.description ? (
             <div className="flex items-center gap-2 text-[13px] text-[#4d5156] dark:text-gray-300">
                <div className="flex-shrink-0 w-4 h-4 rounded-full bg-[#e8f0fe] flex items-center justify-center text-[#1a73e8]">
                    <User size={10} />
                </div>
                <span className="italic line-clamp-1">"{result.description}"</span>
             </div>
           ) : (
             <div className="text-[13px] text-[#70757a] dark:text-gray-400">Livraison disponible</div>
           )}
        </div>
      </div>

      {/* Image — Masquée si absente */}
      {displayImage && (
        <div className="flex-shrink-0 w-24 h-24 rounded-xl overflow-hidden bg-gray-50 border border-[#dadce0] dark:border-gray-700">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img
            src={displayImage}
            alt={result.title || result.name}
            className="w-full h-full object-cover"
            onError={(e) => { (e.target as HTMLImageElement).parentElement!.style.display = "none"; }}
          />
        </div>
      )}
    </div>
  );
}
