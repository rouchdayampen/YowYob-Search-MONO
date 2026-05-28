package com.yowyob.auth.domain.port.out;

/**
 * Port de sortie — interface vers l'encodage des mots de passe.
 * Implémenté par {@link com.yowyob.auth.adapter.out.security.BcryptPasswordAdapter}.
 * Découple le domaine de Spring Security BCrypt.
 */
public interface PasswordPort {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
