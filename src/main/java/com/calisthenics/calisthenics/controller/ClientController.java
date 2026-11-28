package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.ClientProfileRequest;
import com.calisthenics.calisthenics.dto.ClientProfileResponse;
import com.calisthenics.calisthenics.security.CurrentUser;
import com.calisthenics.calisthenics.service.ClientProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {
    private final ClientProfileService service;

    public ClientController(ClientProfileService service) { this.service = service; }

    @PostMapping
    public ClientProfileResponse create(@Valid @RequestBody ClientProfileRequest request){
        return this.service.save(request);
    }

    @GetMapping
    public List<ClientProfileResponse> getAll(Authentication authentication){
        return this.service.getAll(CurrentUser.from(authentication));
    }

    @GetMapping("/{id}")
    public ClientProfileResponse getById(@PathVariable Long id, Authentication authentication){
        return this.service.getById(id, CurrentUser.from(authentication));
    }

    @PutMapping("/{id}")
    public ClientProfileResponse update(@PathVariable Long id, @Valid @RequestBody ClientProfileRequest request,
                                        Authentication authentication){
        return service.update(id, request, CurrentUser.from(authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
