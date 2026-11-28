package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.ExerciseRequest;
import com.calisthenics.calisthenics.dto.ExerciseResponse;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.Exercise;
import com.calisthenics.calisthenics.model.ExerciseCategory;
import com.calisthenics.calisthenics.repository.ExerciseCategoryRepository;
import com.calisthenics.calisthenics.repository.ExerciseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExerciseService {
    private final ExerciseCategoryRepository exerciseCategoryRepository;
    private final ExerciseRepository exerciseRepository;

    public ExerciseService(
            ExerciseCategoryRepository exerciseCategoryRepository,
            ExerciseRepository exerciseRepository){
        this.exerciseCategoryRepository = exerciseCategoryRepository;
        this.exerciseRepository = exerciseRepository;
    }

    public ExerciseResponse save(ExerciseRequest request){
        ExerciseCategory exerciseCategory = exerciseCategoryRepository.findById(request.getExerciseCategoryId())
                .orElseThrow(()->new ResourceNotFoundException("Type Routine not found"));

        Exercise exercise = new Exercise();
        exercise.setName(request.getName());
        exercise.setDescription(request.getDescription());
        exercise.setIsWeighted(request.getWeighted());
        exercise.setExerciseCategory(exerciseCategory);

        return ExerciseResponse.from(exerciseRepository.save(exercise));
    }

    public List<ExerciseResponse> getAll(){
        return exerciseRepository.findAll().stream().map(ExerciseResponse::from).toList();
    }

    public Exercise findById(Long id){
        return exerciseRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Routine not found"));
    }

    public ExerciseResponse update(Long id, ExerciseRequest request){
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Routine not found"));
        ExerciseCategory exerciseCategory = exerciseCategoryRepository.findById(request.getExerciseCategoryId())
                .orElseThrow(()->new ResourceNotFoundException("Routine not found"));

        exercise.setName(request.getName());
        exercise.setDescription(request.getDescription());
        exercise.setExerciseCategory(exerciseCategory);
        exercise.setIsWeighted(request.getWeighted());

        return ExerciseResponse.from(exerciseRepository.save(exercise));
    }

    public void delete(Long id){
        Exercise existing = findById(id);
        exerciseRepository.delete(existing);
    }
}
