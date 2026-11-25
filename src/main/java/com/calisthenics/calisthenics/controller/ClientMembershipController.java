package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.ClientMembershipRequest;
import com.calisthenics.calisthenics.dto.ClientMembershipResponse;
import com.calisthenics.calisthenics.service.ClientMembershipService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/client-memberships")
public class ClientMembershipController {
    private final ClientMembershipService service;
    public ClientMembershipController(ClientMembershipService service){ this.service = service; }

    @PostMapping
    public ClientMembershipResponse create(@Valid @RequestBody ClientMembershipRequest request){
        return this.service.save(request);
    }

    @GetMapping
    public List<ClientMembershipResponse> getAll(@RequestParam(required = false) Long userId){
        return this.service.getAll(userId);
    }

    @GetMapping("/{id}")
    public ClientMembershipResponse getById(@PathVariable Long id){ return this.service.getById(id); }

    @PutMapping("/{id}")
    public ClientMembershipResponse update(@PathVariable Long id, @Valid @RequestBody ClientMembershipRequest request){
        return this.service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
