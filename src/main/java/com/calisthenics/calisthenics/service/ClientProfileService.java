package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.ClientProfile;
import com.calisthenics.calisthenics.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientProfileService {
    private final ClientRepository repository;

    public ClientProfileService(ClientRepository repository){
        this.repository = repository;
    }

    public ClientProfile save(ClientProfile clientProfile){ return this.repository.save(clientProfile); }

    public List<ClientProfile> getAll(){ return repository.findAll(); }

    public ClientProfile findById(Long id){
        return repository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));
    }

    public ClientProfile update(Long id, ClientProfile clientProfile){
        ClientProfile existing = findById(id);

        existing.setBirthdate(clientProfile.getBirthdate());
        existing.setInitialWeight(clientProfile.getInitialWeight());
        existing.setInitialHeight(clientProfile.getInitialHeight());

        return repository.save(existing);
    }

    public void delete(Long id){
        ClientProfile existing = findById(id);
        repository.delete(existing);
    }
}
