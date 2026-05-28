package com.yowyob.auth.adapter.out.security;

import com.yowyob.auth.domain.port.out.PasswordPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adaptateur de sécurité implémentant {@link PasswordPort}.
 * Encapsule Spring Security {@link PasswordEncoder} (BCrypt).
 * Le domaine ne connaît pas BCrypt.
 */
@Component
@RequiredArgsConstructor
public class BcryptPasswordAdapter implements PasswordPort {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
