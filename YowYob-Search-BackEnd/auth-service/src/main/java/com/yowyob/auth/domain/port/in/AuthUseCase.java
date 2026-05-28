package com.yowyob.auth.domain.port.in;

import com.yowyob.auth.dto.AuthResponse;
import com.yowyob.auth.dto.GoogleLoginRequest;
import com.yowyob.auth.dto.LoginRequest;
import com.yowyob.auth.dto.RegisterRequest;

/**
 * Port d'entrée (Use Case) — définit toutes les opérations d'authentification.
 * Implémenté par {@link com.yowyob.auth.domain.service.AuthApplicationService}.
 * Appelé par les adaptateurs REST.
 */
public interface AuthUseCase {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse googleLogin(GoogleLoginRequest request);
}
