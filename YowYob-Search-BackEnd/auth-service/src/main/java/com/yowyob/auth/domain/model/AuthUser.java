package com.yowyob.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité AuthUser du domaine.
 * POJO pur — 0 dépendance JPA / Spring Security.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {

    private UUID id;
    private String name;
    private String email;
    private String password; // mot de passe encodé
    private String phone;
    private String avatarUrl;
    private Role role;
    private Boolean emailVerified;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private Role defaultRole = Role.USER;

    @Builder.Default
    private Status defaultStatus = Status.ACTIVE;

    public enum Role {
        USER, ADMIN, MERCHANT
    }

    public enum Status {
        ACTIVE, INACTIVE, BANNED
    }
}
