package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.model.ClientProfile;
import com.calisthenics.calisthenics.service.ClientProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {
    private final ClientProfileService service;

    public ClientController(ClientProfileService service) { this.service = service; }

    @PostMapping
    public ClientProfile create(@RequestBody ClientProfile clientProfile){
        return this.service.save(clientProfile);
    }

    @GetMapping
    public List<ClientProfile> getAll(){ return this.service.getAll(); }

    @PutMapping("/{id}")
    public ClientProfile update(@PathVariable Long id, @RequestBody ClientProfile clientProfile){
        return service.update(id, clientProfile);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
