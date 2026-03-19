package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.model.ExerciseCategory;
import com.calisthenics.calisthenics.service.ExerciseCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exercise-category")
public class ExerciseCategoryController {

    private final ExerciseCategoryService service;

    public ExerciseCategoryController(ExerciseCategoryService service){
        this.service = service;
    }

    @PostMapping
    public ExerciseCategory create(@RequestBody ExerciseCategory exerciseCategory){
        return service.save(exerciseCategory);
    }

    @GetMapping
    public List<ExerciseCategory> getAll(){
        return service.getAll();
    }

    @PutMapping("/{id}")
    public ExerciseCategory update(@PathVariable Long id, @RequestBody ExerciseCategory exerciseCategory){
        return service.update(id, exerciseCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
