package com.yowyob.auth.domain.port.out;

import java.util.UUID;

/**
 * Port de sortie — interface vers la génération de tokens JWT.
 * Implémenté par {@link com.yowyob.auth.adapter.out.security.JwtAdapter}.
 * Le domaine ne connaît pas JJWT.
 */
public interface TokenPort {

    String generateAccessToken(UUID userId, String email, String role);

    String generateRefreshToken(UUID userId);

    Long getExpirationTime();
}
