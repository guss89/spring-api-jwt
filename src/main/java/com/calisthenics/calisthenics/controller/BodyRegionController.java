package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.BodyRegionRequest;
import com.calisthenics.calisthenics.dto.BodyRegionResponse;
import com.calisthenics.calisthenics.service.BodyRegionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/body-regions")
public class BodyRegionController {
    private final BodyRegionService service;

    public BodyRegionController(BodyRegionService service){
        this.service = service;
    }

    @PostMapping
    public BodyRegionResponse create(@Valid @RequestBody BodyRegionRequest request){
        return service.save(request);
    }

    @GetMapping
    public List<BodyRegionResponse> getAll(){
        return service.getAll();
    }

    @PutMapping("/{id}")
    public BodyRegionResponse update(@PathVariable Long id, @Valid @RequestBody BodyRegionRequest request){
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
