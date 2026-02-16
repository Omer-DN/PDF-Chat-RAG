package org.handson.ragllm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "google_sub")
})
public class User {

    /** Placeholder for users who signed in via Google (no password). */
    public static final String OAUTH_NO_PASSWORD = "OAUTH_GOOGLE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "google_sub", unique = true, length = 255)
    private String googleSub;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected User() {}

    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.googleSub = null;
        this.createdAt = LocalDateTime.now();
    }

    /** Constructor for Google OAuth users (no password). */
    public static User fromGoogle(String email, String username, String googleSub) {
        User u = new User();
        u.username = username;
        u.email = email;
        u.passwordHash = OAUTH_NO_PASSWORD;
        u.googleSub = googleSub;
        u.createdAt = LocalDateTime.now();
        return u;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getGoogleSub() { return googleSub; }
    public void setGoogleSub(String googleSub) { this.googleSub = googleSub; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
