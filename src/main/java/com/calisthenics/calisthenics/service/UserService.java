package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository){
        this.repository = repository;
    }

    public User save(User user){
        return this.repository.save(user);
    }

    public List<User> getAll() { return repository.findAll(); }

    public User findById(Long id){
        return repository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Client Profile not found"));
    }

    public User update(Long id, User user){
        User existing = findById(id);

        existing.setName(user.getName());
        existing.setLastName(user.getLastName());
        existing.setUsername(user.getUsername());
        existing.setEmail(user.getEmail());
        existing.setPhone(user.getPhone());
        existing.setRole(user.getRole());
        existing.setActive(user.getActive());

        return repository.save(existing);
    }

    public void delete(Long id){
        User existing = findById(id);

        repository.delete(existing);
    }
}
