package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.UserRequest;
import com.calisthenics.calisthenics.dto.UserResponse;
import com.calisthenics.calisthenics.dto.UserUpdateRequest;
import com.calisthenics.calisthenics.exception.ConflictException;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.model.catalogs.Role;
import com.calisthenics.calisthenics.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse create(UserRequest request) {
        if (repository.findByUsername(request.getUsername()).isPresent()) {
            throw new ConflictException("Username already exists");
        }
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        return UserResponse.from(repository.save(user));
    }

    public List<UserResponse> getAll() {
        return repository.findAll().stream().map(UserResponse::from).toList();
    }

    public UserResponse getById(Long id) {
        return UserResponse.from(findEntity(id));
    }

    public UserResponse update(Long id, UserUpdateRequest request, Long currentUserId) {
        User existing = findEntity(id);

        boolean isSelf = existing.getId() != null && existing.getId().equals(currentUserId);
        if (isSelf) {
            if (request.getRole() != Role.ADMIN) {
                throw new IllegalArgumentException("An admin cannot remove their own ADMIN role");
            }
            if (Boolean.FALSE.equals(request.getActive())) {
                throw new IllegalArgumentException("An admin cannot deactivate their own account");
            }
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            repository.findByUsername(request.getUsername())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> {
                        throw new ConflictException("Username already exists");
                    });
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            repository.findByEmail(request.getEmail())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> {
                        throw new ConflictException("Email already registered");
                    });
        }

        existing.setName(request.getName());
        existing.setLastName(request.getLastName());
        existing.setUsername(request.getUsername());
        existing.setEmail(request.getEmail());
        existing.setPhone(request.getPhone());
        existing.setRole(request.getRole());
        if (request.getActive() != null) {
            existing.setActive(request.getActive());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existing.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        return UserResponse.from(repository.save(existing));
    }

    /** Baja lógica: active = false. No se borra la fila porque otras tablas apuntan al usuario. */
    public void deactivate(Long id, Long currentUserId) {
        User existing = findEntity(id);

        if (existing.getId() != null && existing.getId().equals(currentUserId)) {
            throw new IllegalArgumentException("An admin cannot deactivate their own account");
        }

        existing.setActive(false);
        repository.save(existing);
    }

    private User findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
