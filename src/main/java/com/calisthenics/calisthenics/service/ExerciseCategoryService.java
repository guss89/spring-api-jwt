package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.ExerciseCategoryRequest;
import com.calisthenics.calisthenics.dto.ExerciseCategoryResponse;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.ExerciseCategory;
import com.calisthenics.calisthenics.repository.ExerciseCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExerciseCategoryService {

    private final ExerciseCategoryRepository repository;

    public ExerciseCategoryService( ExerciseCategoryRepository repository){
        this.repository = repository;
    }

    public ExerciseCategoryResponse save(ExerciseCategoryRequest request){
        ExerciseCategory exerciseCategory = new ExerciseCategory();
        exerciseCategory.setName(request.getName());
        return ExerciseCategoryResponse.from(this.repository.save(exerciseCategory));
    }
    public List<ExerciseCategoryResponse> getAll(){
        return repository.findAll().stream().map(ExerciseCategoryResponse::from).toList();
    }

    public ExerciseCategory findById(Long id){
        return repository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Exercise Category not fount"));
    }
    public ExerciseCategoryResponse update(Long id, ExerciseCategoryRequest request){
        ExerciseCategory existing = findById(id);
        existing.setName(request.getName());
        return ExerciseCategoryResponse.from(repository.save(existing));
    }

    public void delete(Long id){
        ExerciseCategory existing = findById(id);
        repository.delete(existing);
    }
}
