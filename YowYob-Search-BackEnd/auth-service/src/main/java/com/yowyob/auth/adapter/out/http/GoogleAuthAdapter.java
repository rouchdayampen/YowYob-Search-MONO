package com.yowyob.auth.adapter.out.http;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.yowyob.auth.domain.port.out.GoogleAuthPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Adaptateur HTTP implémentant {@link GoogleAuthPort}.
 * Encapsule la librairie Google API Client — le domaine ne la connaît pas.
 */
@Component
@Slf4j
public class GoogleAuthAdapter implements GoogleAuthPort {

    @Value("${google.client-id}")
    private String clientId;

    @Override
    public GoogleUserPayload verify(String tokenString) {
        String actualClientId = (clientId != null && !clientId.isEmpty()) ? clientId
                : "763004243989-g1fftlketknf2ip32f0fsi39ukcqqkq3.apps.googleusercontent.com";
        log.info("Verifying Google Token with Client ID: '{}'", actualClientId);

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(actualClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(tokenString);
            if (idToken == null) {
                throw new IllegalArgumentException("Invalid ID token (verify returned null).");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            return new GoogleUserPayload(email, name);

        } catch (Exception e) {
            log.error("Google token verification failed: ", e);
            throw new RuntimeException("Google token verification failed: " + e.getMessage(), e);
        }
    }
}
