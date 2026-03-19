package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.RoutineRequest;
import com.calisthenics.calisthenics.dto.RoutineResponse;
import com.calisthenics.calisthenics.model.Routine;
import com.calisthenics.calisthenics.service.RoutineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routines")
public class RoutineController {
    private final RoutineService service;
    public RoutineController(RoutineService service){ this.service = service; }

    @PostMapping
    public RoutineResponse create(@RequestBody RoutineRequest routineRequest) { return  this.service.save(routineRequest); }

    @GetMapping
    public List<Routine> getAll(){ return this.service.getAll(); }

    @PutMapping("/{id}")
    public Routine update(@PathVariable Long id, @RequestBody RoutineRequest routineRequest){
        return service.update(id, routineRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
