package org.handson.ragllm.service;

import org.handson.ragllm.model.User;
import org.handson.ragllm.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String username, String email, String password) {
        // ב-Production היינו בודקים כאן אם ה-email כבר תפוס
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return userRepository.save(user);
    }
    public User validateUser(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getPassword().equals(password))
                .orElse(null);
    }
}