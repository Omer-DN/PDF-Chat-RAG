package org.handson.ragllm.controller;

import org.handson.ragllm.dto.UserRegistrationRequest;
import org.handson.ragllm.model.User;
import org.handson.ragllm.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173") // חשוב לחיבור עם React
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody UserRegistrationRequest request) {
        // יצירת משתמש דרך ה-Service (כולל שם, אימייל וסיסמה)
        User newUser = userService.createUser(
                request.username(),
                request.email(),
                request.password()
        );
        return ResponseEntity.ok(newUser);
    }
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        // ב-Service תחפש משתמש לפי מייל ותוודא שהסיסמה תואמת
        User user = userService.validateUser(email, password);

        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}