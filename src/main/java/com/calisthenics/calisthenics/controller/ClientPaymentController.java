package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.ClientPaymentRequest;
import com.calisthenics.calisthenics.dto.ClientPaymentResponse;
import com.calisthenics.calisthenics.security.CurrentUser;
import com.calisthenics.calisthenics.service.ClientPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/client-payments")
public class ClientPaymentController {
    private final ClientPaymentService service;
    public ClientPaymentController(ClientPaymentService service){ this.service = service; }

    @PostMapping
    public ClientPaymentResponse create(@Valid @RequestBody ClientPaymentRequest request, Authentication authentication){
        return this.service.save(request, CurrentUser.from(authentication));
    }

    @GetMapping
    public List<ClientPaymentResponse> getAll(@RequestParam(required = false) Long userId, Authentication authentication){
        return this.service.getAll(userId, CurrentUser.from(authentication));
    }

    @GetMapping("/{id}")
    public ClientPaymentResponse getById(@PathVariable Long id, Authentication authentication){
        return this.service.getById(id, CurrentUser.from(authentication));
    }

    @PutMapping("/{id}")
    public ClientPaymentResponse update(@PathVariable Long id, @Valid @RequestBody ClientPaymentRequest request,
                                        Authentication authentication){
        return this.service.update(id, request, CurrentUser.from(authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication){
        service.delete(id, CurrentUser.from(authentication));
        return ResponseEntity.noContent().build();
    }
}
