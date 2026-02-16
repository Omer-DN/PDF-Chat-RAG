package org.handson.ragllm.service;

import org.handson.ragllm.model.User;
import org.handson.ragllm.repository.UserRepository;
import org.handson.ragllm.security.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("משתמש לא נמצא: " + username));
        return new UserPrincipal(user);
    }

    @Transactional
    public User register(String username, String email, String rawPassword) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("שם משתמש חובה");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("אימייל חובה");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("סיסמה חובה");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("שם המשתמש תפוס");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("המייל כבר רשום במערכת");
        }
        String hash = passwordEncoder.encode(rawPassword);
        if (hash == null || hash.isBlank()) {
            throw new IllegalStateException("שגיאה בשמירת הסיסמה");
        }
        User user = new User(username, email, hash);
        return userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("משתמש לא נמצא"));
    }

    /**
     * Find user by Google sub, or by email (and link google_sub if not set).
     * Otherwise create a new user (OAuth, no password).
     */
    @Transactional
    public User findOrCreateByGoogle(String email, String name, String googleSub) {
        Optional<User> byGoogle = userRepository.findByGoogleSub(googleSub);
        if (byGoogle.isPresent()) return byGoogle.get();
        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            User u = byEmail.get();
            if (u.getGoogleSub() == null) {
                u.setGoogleSub(googleSub);
                return userRepository.save(u);
            }
            return u;
        }
        String username = toUniqueUsername(email, name);
        User user = User.fromGoogle(email, username, googleSub);
        return userRepository.save(user);
    }

    private String toUniqueUsername(String email, String name) {
        String base = (name != null && !name.isBlank()) ? name.trim() : email.split("@")[0];
        base = base.replaceAll("[^a-zA-Z0-9_\\-.]", "_");
        if (base.length() > 90) base = base.substring(0, 90);
        String candidate = base;
        int n = 0;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + "_" + (++n);
        }
        return candidate;
    }
}
