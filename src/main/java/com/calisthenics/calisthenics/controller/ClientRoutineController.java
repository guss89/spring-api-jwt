package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.ClientRoutineRequest;
import com.calisthenics.calisthenics.dto.ClientRoutineResponse;
import com.calisthenics.calisthenics.security.CurrentUser;
import com.calisthenics.calisthenics.service.ClientRoutineService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/client-routine")
public class ClientRoutineController {
    private final ClientRoutineService service;
    public ClientRoutineController(ClientRoutineService service){
        this.service = service;
    }

    @PostMapping
    public ClientRoutineResponse create(@Valid @RequestBody ClientRoutineRequest clientRoutineRequest) { return this.service.save(clientRoutineRequest); }

    @GetMapping("/{id}")
    public ClientRoutineResponse getById(@PathVariable Long id, Authentication authentication){
        return this.service.getById(id, CurrentUser.from(authentication));
    }

    @PutMapping("/{id}")
    public ClientRoutineResponse update(@PathVariable Long id, @Valid @RequestBody ClientRoutineRequest clientRoutineRequest){
        return this.service.update(id, clientRoutineRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        this.service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<ClientRoutineResponse> getAll(Authentication authentication){
        return this.service.getAll(CurrentUser.from(authentication));
    }

}
