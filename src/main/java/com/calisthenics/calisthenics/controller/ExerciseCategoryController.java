package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.ExerciseCategoryRequest;
import com.calisthenics.calisthenics.dto.ExerciseCategoryResponse;
import com.calisthenics.calisthenics.service.ExerciseCategoryService;
import jakarta.validation.Valid;
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
    public ExerciseCategoryResponse create(@Valid @RequestBody ExerciseCategoryRequest request){
        return service.save(request);
    }

    @GetMapping
    public List<ExerciseCategoryResponse> getAll(){
        return service.getAll();
    }

    @PutMapping("/{id}")
    public ExerciseCategoryResponse update(@PathVariable Long id, @Valid @RequestBody ExerciseCategoryRequest request){
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
