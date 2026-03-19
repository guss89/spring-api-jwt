package com.calisthenics.calisthenics.service;

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

    public ExerciseCategory save(ExerciseCategory exerciseCategory){
        return  this.repository.save(exerciseCategory);
    }
    public List<ExerciseCategory> getAll(){
        return repository.findAll();
    }

    public ExerciseCategory findById(Long id){
        return repository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Exercise Category not fount"));
    }
    public ExerciseCategory update(Long id, ExerciseCategory exerciseCategory){
        ExerciseCategory existing = findById(id);
        existing.setName(exerciseCategory.getName());
        return repository.save(existing);
    }

    public void delete(Long id){
        ExerciseCategory existing = findById(id);
        repository.delete(existing);
    }
}
