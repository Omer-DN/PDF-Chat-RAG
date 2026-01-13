package org.handson.ragllm.controller;

import org.handson.ragllm.dto.UserRegistrationRequest;
import org.handson.ragllm.model.User;
import org.handson.ragllm.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody UserRegistrationRequest request) {
        // התיקון: מעבירים 3 פרמטרים כולל password
        User newUser = userService.createUser(request.username(), request.email(), request.password());
        return ResponseEntity.ok(newUser);
    }
}