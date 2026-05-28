package com.yowyob.auth.domain.service;

import com.yowyob.auth.domain.model.AuthUser;
import com.yowyob.auth.domain.port.in.AuthUseCase;
import com.yowyob.auth.domain.port.out.*;
import com.yowyob.auth.dto.AuthResponse;
import com.yowyob.auth.dto.GoogleLoginRequest;
import com.yowyob.auth.dto.LoginRequest;
import com.yowyob.auth.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service applicatif du domaine Auth.
 * Implémente {@link AuthUseCase}.
 * Dépend uniquement des ports de sortie (interfaces) — pas de JPA, pas de Kafka, pas de Spring Security.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthApplicationService implements AuthUseCase {

    private final AuthUserRepository userRepository;
    private final PasswordPort passwordPort;
    private final TokenPort tokenPort;
    private final EventPublisherPort eventPublisherPort;
    private final GoogleAuthPort googleAuthPort;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Starting registration for email: {}", email);

        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed: email already exists: {}", email);
            throw new RuntimeException("Email already exists");
        }

        AuthUser user = AuthUser.builder()
                .name(request.getName())
                .email(email)
                .password(passwordPort.encode(request.getPassword()))
                .role(AuthUser.Role.USER)
                .status(AuthUser.Status.ACTIVE)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);
        log.info("User saved with ID: {}", user.getId());

        try {
            eventPublisherPort.publishUserCreated(user);
        } catch (Exception e) {
            log.error("Failed to publish UserCreated event for {}: {}", email, e.getMessage());
        }

        return buildAuthResponse(user, "User registered successfully");
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Login request for email: {}", email);

        AuthUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found for email: {}", email);
                    return new RuntimeException("Invalid credentials");
                });

        if (!passwordPort.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: password mismatch for email: {}", email);
            throw new RuntimeException("Invalid credentials");
        }

        if (user.getStatus() != AuthUser.Status.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }

        return buildAuthResponse(user, "Login successful");
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        try {
            GoogleAuthPort.GoogleUserPayload payload = googleAuthPort.verify(request.getToken());
            String email = payload.email().trim().toLowerCase();
            String name = payload.name();

            log.info("Google Login for email: {}", email);

            AuthUser user = userRepository.findByEmail(email).orElseGet(() -> {
                log.info("New Google user ({}). Registering...", email);
                AuthUser newUser = AuthUser.builder()
                        .name(name != null ? name : email)
                        .email(email)
                        .password(passwordPort.encode(UUID.randomUUID().toString()))
                        .role(AuthUser.Role.USER)
                        .status(AuthUser.Status.ACTIVE)
                        .emailVerified(true)
                        .build();
                AuthUser saved = userRepository.save(newUser);
                try {
                    eventPublisherPort.publishUserCreated(saved);
                } catch (Exception e) {
                    log.error("Failed to publish event for Google user {}: {}", email, e.getMessage());
                }
                return saved;
            });

            if (user.getStatus() != AuthUser.Status.ACTIVE) {
                throw new RuntimeException("Account is not active");
            }

            return buildAuthResponse(user, "Google Login successful");

        } catch (Exception e) {
            log.error("Google Login failed", e);
            throw new RuntimeException("Google Login failed: " + e.getMessage());
        }
    }

    private AuthResponse buildAuthResponse(AuthUser user, String message) {
        String accessToken = tokenPort.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = tokenPort.generateRefreshToken(user.getId());

        return AuthResponse.builder()
                .success(true)
                .message(message)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(tokenPort.getExpirationTime())
                .user(AuthResponse.UserDto.builder()
                        .id(user.getId().toString())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .build())
                .build();
    }
}
