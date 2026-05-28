package com.yowyob.auth.domain.port.out;

/**
 * Port de sortie — interface vers la vérification des tokens Google.
 * Implémenté par {@link com.yowyob.auth.adapter.out.http.GoogleAuthAdapter}.
 * Le domaine ne connaît pas la librairie Google API Client.
 */
public interface GoogleAuthPort {

    /**
     * Vérifie un token Google et retourne l'email de l'utilisateur.
     * @param token le token Google ID Token
     * @return payload contenant email et nom
     */
    GoogleUserPayload verify(String token);

    record GoogleUserPayload(String email, String name) {}
}
