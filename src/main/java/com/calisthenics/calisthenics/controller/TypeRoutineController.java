package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.model.TypeRoutine;
import com.calisthenics.calisthenics.service.TypeRoutineService;
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
    public TypeRoutine create(@RequestBody TypeRoutine typeRoutine){
        return this.service.save(typeRoutine);
    }

    @GetMapping
    public List<TypeRoutine> getAll(){
        return this.service.getAll();
    }

    @PutMapping("/{id}")
    public TypeRoutine update(@PathVariable Long id, @RequestBody TypeRoutine typeRoutine){
        return service.update(id, typeRoutine);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
