package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.RoutineExerciseRequest;
import com.calisthenics.calisthenics.dto.RoutineExerciseResponse;
import com.calisthenics.calisthenics.service.RoutineExerciseService;
import com.calisthenics.calisthenics.service.RoutineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routine-exercise")
public class RoutineExerciseController {
    private final RoutineExerciseService service;
    public RoutineExerciseController(RoutineExerciseService service){ this.service = service; }

    @PostMapping
    public RoutineExerciseResponse create(@RequestBody RoutineExerciseRequest routineExerciseRequest) { return this.service.save(routineExerciseRequest);}

    @GetMapping
    public List<RoutineExerciseResponse> getAll(){ return this.service.getAll(); }

    @PutMapping("/{id}")
    public RoutineExerciseResponse update(@PathVariable Long id, @RequestBody RoutineExerciseRequest routineExerciseRequest){
        return service.update(id, routineExerciseRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
