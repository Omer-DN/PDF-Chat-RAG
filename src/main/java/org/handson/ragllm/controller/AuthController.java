package org.handson.ragllm.controller;

import org.handson.ragllm.model.User;
import org.handson.ragllm.security.JwtUtil;
import org.handson.ragllm.security.UserPrincipal;
import org.handson.ragllm.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
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

    public static class RegisterRequest {
        @NotBlank(message = "שם משתמש חובה")
        private String username;
        @NotBlank(message = "מייל חובה")
        private String email;
        @NotBlank(message = "סיסמה חובה")
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
