package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.ExerciseRequest;
import com.calisthenics.calisthenics.dto.ExerciseResponse;
import com.calisthenics.calisthenics.model.Exercise;
import com.calisthenics.calisthenics.service.ExerciseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exercises")
public class ExerciseController {
    private final ExerciseService service;
    public ExerciseController (ExerciseService service){ this.service = service; }

    @PostMapping
    public ExerciseResponse create(@RequestBody ExerciseRequest exerciseRequest) {return this.service.save(exerciseRequest);}

    @GetMapping
    public List<Exercise> getAll() {return this.service.getAll(); }

    @PutMapping({"/{id}"})
    public Exercise update(@PathVariable Long id, @RequestBody ExerciseRequest exerciseRequest){
        return service.update(id, exerciseRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
