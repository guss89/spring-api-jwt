package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.LoginRequest;
import com.calisthenics.calisthenics.dto.LoginResponse;
import com.calisthenics.calisthenics.dto.RegisterRequest;
import com.calisthenics.calisthenics.exception.ConflictException;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.model.catalogs.Role;
import com.calisthenics.calisthenics.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public User register(RegisterRequest request){
        //same rule and order as UserService.create (duplicates -> 409)
        if(userRepository.findByUsername(request.getUsername()).isPresent()){
            throw new ConflictException("Username already exists");
        }

        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new ConflictException("Email already registered");
        }

        User user = new User();

        user.setName(request.getName());


        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(Role.CLIENT); // el registro público siempre crea CLIENT; se ignora el rol enviado
        user.setActive(true);

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request){

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String token = jwtService.generateToken(user.getId(), user.getRole().name());

        return new LoginResponse(token, user.getRole().name(), user.getId());
    }
}
