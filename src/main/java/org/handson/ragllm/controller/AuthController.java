package org.handson.ragllm.controller;

import org.handson.ragllm.model.User;
import org.handson.ragllm.security.JwtUtil;
import org.handson.ragllm.security.UserPrincipal;
import org.handson.ragllm.service.GoogleTokenVerifier;
import org.handson.ragllm.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final GoogleTokenVerifier googleTokenVerifier;

    public AuthController(UserService userService, JwtUtil jwtUtil, AuthenticationManager authenticationManager,
                          GoogleTokenVerifier googleTokenVerifier) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request.getUsername(), request.getEmail(), request.getPassword());
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "userId", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            String token = jwtUtil.generateToken(principal.getId(), principal.getUsername());
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "userId", principal.getId(),
                    "username", principal.getUsername(),
                    "email", principal.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "שם משתמש או סיסמה שגויים"));
        }
    }

    /**
     * Sign in / register with Google (Gmail).
     * Body: { "idToken": "<Google ID token from frontend>" }.
     * Returns same shape as login (token, userId, username, email) or 400/401 if invalid.
     */
    @PostMapping("/google")
    public ResponseEntity<?> google(@RequestBody Map<String, String> body) {
        String idToken = body != null ? body.get("idToken") : null;
        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "חסר idToken"));
        }
        if (!googleTokenVerifier.isConfigured()) {
            return ResponseEntity.status(503).body(Map.of("message", "התחברות עם Google אינה מופעלת בשרת"));
        }
        return googleTokenVerifier.verify(idToken)
                .map(info -> {
                    User user = userService.findOrCreateByGoogle(info.email(), info.name(), info.googleSub());
                    String token = jwtUtil.generateToken(user.getId(), user.getUsername());
                    return ResponseEntity.ok(Map.<String, Object>of(
                            "token", token,
                            "userId", user.getId(),
                            "username", user.getUsername(),
                            "email", user.getEmail()
                    ));
                })
                .orElse(ResponseEntity.status(401).body(Map.of("message", "טוקן Google לא תקף או שפג תוקפו")));
    }

    public static class RegisterRequest {
        @NotBlank(message = "שם משתמש חובה")
        @Size(max = 100)
        private String username;
        @NotBlank(message = "אימייל חובה (לצרכים בהמשך)")
        @Size(max = 255)
        private String email;
        @NotBlank(message = "סיסמה חובה")
        @Size(min = 4, max = 100, message = "סיסמה בין 4 ל־100 תווים")
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /** כניסה רק עם שם משתמש וסיסמה */
    public static class LoginRequest {
        @NotBlank(message = "שם משתמש חובה")
        private String username;
        @NotBlank(message = "סיסמה חובה")
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
