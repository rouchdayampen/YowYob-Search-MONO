package com.yowyob.search.domain.service;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Parseur sémantique de requêtes de recherche.
 * Déplacé de service/ vers domain/service/ — logique métier pure, 0 dépendance framework.
 */
@Component
public class KeywordParser {

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "de","des","du","la","le","les","un","une","dans","pour","à","a","au","aux",
            "je","veux","recherche","trouver","l","y","en","sur","avec","chez","par",
            "se","sa","son","ses","mon","ma","mes","want","wanting","pres","proche","autour","loin","tres","moi"));

    private static final Set<String> CAMEROON_CITIES = new HashSet<>(Arrays.asList(
            "douala","yaoundé","yaounde","buea","bafoussam","bamenda","garoua",
            "limbe","tiko","kumba","ngaoundere","bertoua","batouri","ebolowa",
            "dschang","foumban","koutaba","banyo","nkongsamba","edea","kribi","campo"));

    private static final Map<String, String> CATEGORY_MAPPING = new HashMap<>();
    static {
        String immo = "Immobilier";
        for (String k : new String[]{"maison","logement","appartement","studio","chambre","villa","terrain","louer","hotel","auberge"})
            CATEGORY_MAPPING.put(k, immo);
        String resto = "Restaurant";
        for (String k : new String[]{"restaurant","resto","manger","faim","nourriture","repas","pizza","burger","bar","cafe","boulangerie"})
            CATEGORY_MAPPING.put(k, resto);
        String elec = "Electronique";
        for (String k : new String[]{"telephone","phone","smartphone","iphone","samsung","ordinateur","pc","laptop","tablette","tv"})
            CATEGORY_MAPPING.put(k, elec);
        String mode = "Mode";
        for (String k : new String[]{"vetement","habit","chaussure","robe","pantalon","chemise","sac","bijou","montre"})
            CATEGORY_MAPPING.put(k, mode);
        String auto = "Automobile";
        for (String k : new String[]{"voiture","auto","vehicule","taxi","transport","mecano","garage"})
            CATEGORY_MAPPING.put(k, auto);
        String beauty = "Beaute";
        for (String k : new String[]{"coiffure","salon","soin","massage","onglerie","maquillage","parfum"})
            CATEGORY_MAPPING.put(k, beauty);
    }

    private static final Pattern NON_LETTER = Pattern.compile("[^\\p{L}\\p{Nd}]+");

    public static class ParsedQueryResult {
        public String query;
        public String extractedCity;
        public String inferredCategory;
        public Double proximityRadius;
        public boolean isProximitySearch;

        public ParsedQueryResult(String q, String city, String cat, Double radius, boolean proximity) {
            this.query = q; this.extractedCity = city; this.inferredCategory = cat;
            this.proximityRadius = radius; this.isProximitySearch = proximity;
        }
    }

    public ParsedQueryResult parseWithCity(String input) {
        String q = buildQuery(input);
        String city = extractCity(input);
        String cat = extractCategory(input);
        Double radius = extractProximityRadius(input);
        return new ParsedQueryResult(q, city, cat, radius, radius != null);
    }

    public List<String> extractKeywords(String input) {
        if (input == null) return Collections.emptyList();
        String cleaned = NON_LETTER.matcher(input.trim().toLowerCase().replace("'","'")).replaceAll(" ");
        List<String> tokens = new ArrayList<>();
        for (String p : cleaned.split("\\s+")) {
            if (p.isEmpty() || STOPWORDS.contains(p) || CAMEROON_CITIES.contains(p)) continue;
            tokens.add(p.length() > 3 && p.endsWith("s") ? p.substring(0, p.length()-1) : p);
        }
        return tokens;
    }

    public String buildQuery(String input) {
        List<String> keys = extractKeywords(input);
        return keys.isEmpty() ? (input == null ? "" : input.trim()) : String.join(" ", keys);
    }

    public String extractCity(String input) {
        if (input == null) return null;
        String cleaned = NON_LETTER.matcher(input.trim().toLowerCase()).replaceAll(" ");
        for (String p : cleaned.split("\\s+"))
            if (CAMEROON_CITIES.contains(p)) return p.substring(0,1).toUpperCase() + p.substring(1);
        return null;
    }

    public String extractCategory(String input) {
        for (String kw : extractKeywords(input))
            if (CATEGORY_MAPPING.containsKey(kw)) return CATEGORY_MAPPING.get(kw);
        return null;
    }

    public Double extractProximityRadius(String input) {
        if (input == null) return null;
        String c = NON_LETTER.matcher(input.trim().toLowerCase()).replaceAll(" ");
        if (c.contains("tres") && c.contains("loin")) return 50.0;
        if (c.contains("tres") && c.contains("pres")) return 2.0;
        if (c.contains("proximite")) return 7.0;
        if (c.contains("pres") && (c.contains("chez") || c.contains("moi"))) return 5.0;
        if (c.contains("proche") || c.contains("autour")) return 5.0;
        if (c.contains("loin")) return 20.0;
        return null;
    }
}
