package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.LoginRequest;
import com.calisthenics.calisthenics.dto.RegisterRequest;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public  AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request){

        User user = authService.register(request);

        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request){

        String token = authService.login(request);

        return ResponseEntity.ok(token);
    }
}
