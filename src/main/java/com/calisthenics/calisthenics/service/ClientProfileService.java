package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.ClientProfileRequest;
import com.calisthenics.calisthenics.dto.ClientProfileResponse;
import com.calisthenics.calisthenics.exception.ConflictException;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.ClientProfile;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.model.catalogs.Role;
import com.calisthenics.calisthenics.repository.ClientRepository;
import com.calisthenics.calisthenics.repository.UserRepository;
import com.calisthenics.calisthenics.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientProfileService {
    private final ClientRepository repository;
    private final UserRepository userRepository;

    public ClientProfileService(ClientRepository repository, UserRepository userRepository){
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public ClientProfileResponse save(ClientProfileRequest request){
        User user = findClient(request.getUserId());

        if (repository.existsByUserId(user.getId())) {
            throw new ConflictException("Client profile already exists for this user");
        }

        ClientProfile profile = new ClientProfile();
        profile.setUser(user);
        profile.setBirthDate(request.getBirthDate());
        profile.setInitialWeight(request.getInitialWeight());
        profile.setInitialHeight(request.getInitialHeight());

        return ClientProfileResponse.from(repository.save(profile));
    }

    public List<ClientProfileResponse> getAll(CurrentUser caller){
        if (caller.admin()) {
            return repository.findAll().stream().map(ClientProfileResponse::from).toList();
        }
        return repository.findByUserId(caller.id()).stream().map(ClientProfileResponse::from).toList();
    }

    public ClientProfileResponse getById(Long id, CurrentUser caller){
        ClientProfile profile = findById(id);
        caller.requireAccessTo(profile.getUser().getId());
        return ClientProfileResponse.from(profile);
    }

    public ClientProfile findById(Long id){
        return repository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Client profile not found"));
    }

    public ClientProfileResponse update(Long id, ClientProfileRequest request, CurrentUser caller){
        ClientProfile existing = findById(id);
        caller.requireAccessTo(existing.getUser().getId());

        if (!existing.getUser().getId().equals(request.getUserId())) {
            throw new IllegalArgumentException("Client profile cannot be reassigned to another user");
        }

        existing.setBirthDate(request.getBirthDate());
        existing.setInitialWeight(request.getInitialWeight());
        existing.setInitialHeight(request.getInitialHeight());

        return ClientProfileResponse.from(repository.save(existing));
    }

    public void delete(Long id){
        ClientProfile existing = findById(id);
        repository.delete(existing);
    }

    private User findClient(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() != Role.CLIENT) {
            throw new IllegalArgumentException("User must have role CLIENT");
        }
        return user;
    }
}
