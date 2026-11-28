package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.TypeRoutineRequest;
import com.calisthenics.calisthenics.dto.TypeRoutineResponse;
import com.calisthenics.calisthenics.service.TypeRoutineService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/type-routine")
public class TypeRoutineController {
    private final TypeRoutineService service;

    public TypeRoutineController(TypeRoutineService service){
        this.service = service;
    }

    @PostMapping
    public TypeRoutineResponse create(@Valid @RequestBody TypeRoutineRequest request){
        return service.save(request);
    }

    @GetMapping
    public List<TypeRoutineResponse> getAll(){
        return service.getAll();
    }

    @PutMapping("/{id}")
    public TypeRoutineResponse update(@PathVariable Long id, @Valid @RequestBody TypeRoutineRequest request){
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
