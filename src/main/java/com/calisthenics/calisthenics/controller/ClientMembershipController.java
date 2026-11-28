package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.ClientMembershipRequest;
import com.calisthenics.calisthenics.dto.ClientMembershipResponse;
import com.calisthenics.calisthenics.security.CurrentUser;
import com.calisthenics.calisthenics.service.ClientMembershipService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/client-memberships")
public class ClientMembershipController {
    private final ClientMembershipService service;
    public ClientMembershipController(ClientMembershipService service){ this.service = service; }

    @PostMapping
    public ClientMembershipResponse create(@Valid @RequestBody ClientMembershipRequest request,
                                           Authentication authentication){
        return this.service.save(request, CurrentUser.from(authentication));
    }

    @GetMapping
    public List<ClientMembershipResponse> getAll(@RequestParam(required = false) Long userId,
                                                 Authentication authentication){
        return this.service.getAll(userId, CurrentUser.from(authentication));
    }

    @GetMapping("/{id}")
    public ClientMembershipResponse getById(@PathVariable Long id, Authentication authentication){
        return this.service.getById(id, CurrentUser.from(authentication));
    }

    @PutMapping("/{id}")
    public ClientMembershipResponse update(@PathVariable Long id, @Valid @RequestBody ClientMembershipRequest request,
                                           Authentication authentication){
        return this.service.update(id, request, CurrentUser.from(authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication){
        service.delete(id, CurrentUser.from(authentication));
        return ResponseEntity.noContent().build();
    }
}
