package org.handson.ragllm.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

/**
 * Verifies Google ID tokens (from "Sign in with Google" on the frontend)
 * and returns user info: email, name, sub.
 */
@Service
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(@Value("${app.google.client-id:}") String clientId) {
        if (clientId == null || clientId.isBlank()) {
            this.verifier = null;
            return;
        }
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    /**
     * @return Optional with (email, name, googleSub) or empty if token invalid / Google not configured
     */
    public Optional<GoogleUserInfo> verify(String idToken) {
        if (verifier == null || idToken == null || idToken.isBlank()) {
            return Optional.empty();
        }
        try {
            GoogleIdToken idTokenObj = verifier.verify(idToken);
            if (idTokenObj == null) return Optional.empty();
            GoogleIdToken.Payload payload = idTokenObj.getPayload();
            String email = payload.getEmail();
            String sub = payload.getSubject();
            if (email == null || sub == null) return Optional.empty();
            String name = (String) payload.get("name");
            return Optional.of(new GoogleUserInfo(email, name, sub));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean isConfigured() {
        return verifier != null;
    }

    public record GoogleUserInfo(String email, String name, String googleSub) {}
}
