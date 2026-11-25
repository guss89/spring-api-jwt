package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.ClientPaymentRequest;
import com.calisthenics.calisthenics.dto.ClientPaymentResponse;
import com.calisthenics.calisthenics.service.ClientPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/client-payments")
public class ClientPaymentController {
    private final ClientPaymentService service;
    public ClientPaymentController(ClientPaymentService service){ this.service = service; }

    @PostMapping
    public ClientPaymentResponse create(@Valid @RequestBody ClientPaymentRequest request){ return this.service.save(request); }

    @GetMapping
    public List<ClientPaymentResponse> getAll(@RequestParam(required = false) Long userId){
        return this.service.getAll(userId);
    }

    @GetMapping("/{id}")
    public ClientPaymentResponse getById(@PathVariable Long id){ return this.service.getById(id); }

    @PutMapping("/{id}")
    public ClientPaymentResponse update(@PathVariable Long id, @Valid @RequestBody ClientPaymentRequest request){
        return this.service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
