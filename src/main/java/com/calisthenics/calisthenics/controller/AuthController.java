package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.LoginRequest;
import com.calisthenics.calisthenics.dto.LoginResponse;
import com.calisthenics.calisthenics.dto.RegisterRequest;
import com.calisthenics.calisthenics.dto.UserResponse;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.service.AuthService;
import jakarta.validation.Valid;
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
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request){

        User user = authService.register(request);

        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }
}
