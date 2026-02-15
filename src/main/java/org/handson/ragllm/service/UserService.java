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
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("שם המשתמש תפוס");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("המייל כבר רשום במערכת");
        }
        String hash = passwordEncoder.encode(rawPassword);
        User user = new User(username, email, hash);
        return userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("משתמש לא נמצא"));
    }
}
