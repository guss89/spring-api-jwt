package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.UserRequest;
import com.calisthenics.calisthenics.dto.UserResponse;
import com.calisthenics.calisthenics.dto.UserUpdateRequest;
import com.calisthenics.calisthenics.security.CurrentUser;
import com.calisthenics.calisthenics.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) { this.service = service; }

    @GetMapping
    public List<UserResponse> getAll(){
        return service.getAll();
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id,
                               @Valid @RequestBody UserUpdateRequest request,
                               Authentication authentication) {
        return service.update(id, request, CurrentUser.from(authentication).id());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id, Authentication authentication) {
        service.deactivate(id, CurrentUser.from(authentication).id());
        return ResponseEntity.noContent().build();
    }
}
