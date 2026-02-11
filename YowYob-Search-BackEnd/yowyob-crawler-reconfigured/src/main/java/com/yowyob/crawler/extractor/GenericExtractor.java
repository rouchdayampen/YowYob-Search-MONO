package com.yowyob.crawler.extractor;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Extracteur générique pour n'importe quel site web
 */
@Component
public class GenericExtractor implements SiteExtractor {

    @Override
    public boolean canHandle(String url) {
        // Peut gérer n'importe quelle URL
        return true;
    }

    @Override
    public int getPriority() {
        // Priorité la plus basse (utilisé en dernier recours)
        return Integer.MAX_VALUE;
    }

    @Override
    public Map<String, Object> extract(Document document, String url) {
        Map<String, Object> data = new HashMap<>();
        
        // Métadonnées basiques
        data.put("title", extractTitle(document));
        data.put("description", extractDescription(document));
        data.put("keywords", extractKeywords(document));
        data.put("language", extractLanguage(document));
        
        // Contenu
        data.put("headings", extractHeadings(document));
        data.put("links", extractLinks(document, url));
        data.put("images", extractImages(document));
        data.put("textContent", extractTextContent(document));
        
        // Détection d'éléments de produits (pour e-commerce)
        data.put("products", extractProducts(document));
        
        return data;
    }

    private String extractTitle(Document document) {
        // Essayer og:title
        Element ogTitle = document.selectFirst("meta[property=og:title]");
        if (ogTitle != null && ogTitle.hasAttr("content")) {
            return ogTitle.attr("content");
        }
        
        // Essayer twitter:title
        Element twitterTitle = document.selectFirst("meta[name=twitter:title]");
        if (twitterTitle != null && twitterTitle.hasAttr("content")) {
            return twitterTitle.attr("content");
        }
        
        // Sinon le title HTML
        return document.title();
    }

    private String extractDescription(Document document) {
        Element desc = document.selectFirst("meta[name=description]");
        if (desc != null && desc.hasAttr("content")) {
            return desc.attr("content");
        }
        
        Element ogDesc = document.selectFirst("meta[property=og:description]");
        if (ogDesc != null && ogDesc.hasAttr("content")) {
            return ogDesc.attr("content");
        }
        
        return null;
    }

    private String extractKeywords(Document document) {
        Element keywords = document.selectFirst("meta[name=keywords]");
        if (keywords != null && keywords.hasAttr("content")) {
            return keywords.attr("content");
        }
        return null;
    }

    private String extractLanguage(Document document) {
        Element html = document.selectFirst("html");
        if (html != null && html.hasAttr("lang")) {
            return html.attr("lang");
        }
        return null;
    }

    private Map<String, List<String>> extractHeadings(Document document) {
        Map<String, List<String>> headings = new HashMap<>();
        
        for (int i = 1; i <= 6; i++) {
            Elements hElements = document.select("h" + i);
            List<String> hTexts = new ArrayList<>();
            for (Element h : hElements) {
                String text = h.text().trim();
                if (!text.isEmpty()) {
                    hTexts.add(text);
                }
            }
            if (!hTexts.isEmpty()) {
                headings.put("h" + i, hTexts);
            }
        }
        
        return headings;
    }

    private List<String> extractLinks(Document document, String baseUrl) {
        List<String> links = new ArrayList<>();
        Elements linkElements = document.select("a[href]");
        
        for (Element link : linkElements) {
            String href = link.absUrl("href");
            if (!href.isEmpty() && href.startsWith("http") && !links.contains(href)) {
                links.add(href);
            }
        }
        
        return links;
    }

    private List<Map<String, String>> extractImages(Document document) {
        List<Map<String, String>> images = new ArrayList<>();
        Elements imgElements = document.select("img[src]");
        
        for (Element img : imgElements) {
            Map<String, String> imageData = new HashMap<>();
            imageData.put("src", img.absUrl("src"));
            imageData.put("alt", img.attr("alt"));
            images.add(imageData);
        }
        
        return images;
    }

    private String extractTextContent(Document document) {
        // Essayer de trouver le contenu principal
        Element main = document.selectFirst("main, article, .content, #content, .main");
        
        if (main != null) {
            return main.text();
        }
        
        // Sinon retourner le texte du body (limité)
        Element body = document.body();
        if (body != null) {
            String text = body.text();
            // Limiter à 1000 caractères pour ne pas surcharger
            return text.length() > 1000 ? text.substring(0, 1000) + "..." : text;
        }
        
        return "";
    }

    private List<Map<String, Object>> extractProducts(Document document) {
        List<Map<String, Object>> products = new ArrayList<>();
        
        // Chercher des éléments qui ressemblent à des produits
        Elements productElements = document.select(
            ".product, .item, [class*=product], [class*=item], " +
            "[data-product], [data-item], article.product"
        );
        
        for (Element productEl : productElements) {
            Map<String, Object> product = new HashMap<>();
            
            // Nom du produit
            Element nameEl = productEl.selectFirst("h1, h2, h3, h4, .name, .title, [class*=name], [class*=title]");
            if (nameEl != null) {
                product.put("name", nameEl.text());
            }
            
            // Prix
            Element priceEl = productEl.selectFirst(".price, [class*=price], [data-price]");
            if (priceEl != null) {
                product.put("price", priceEl.text());
            }
            
            // Description
            Element descEl = productEl.selectFirst(".description, [class*=description], p");
            if (descEl != null) {
                product.put("description", descEl.text());
            }
            
            // Lien
            Element linkEl = productEl.selectFirst("a[href]");
            if (linkEl != null) {
                product.put("url", linkEl.absUrl("href"));
            }
            
            // N'ajouter que si on a au moins un nom ou un prix
            if (product.containsKey("name") || product.containsKey("price")) {
                products.add(product);
            }
        }
        
        return products;
    }
}
