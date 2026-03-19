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
        exercise.setWeighted(request.getWeighted());
        exercise.setExerciseCategory(exerciseCategory);

        Exercise saved = exerciseRepository.save(exercise);
        return new ExerciseResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getExerciseCategory().getId(),
                saved.getWeighted()
        );
    }

    public List<Exercise> getAll(){ return exerciseRepository.findAll(); }

    public Exercise findById(Long id){
        return exerciseRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Routine not found"));
    }

    public Exercise update(Long id, ExerciseRequest request){
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Routine not found"));
        ExerciseCategory exerciseCategory = exerciseCategoryRepository.findById(request.getExerciseCategoryId())
                .orElseThrow(()->new ResourceNotFoundException("Routine not found"));

        exercise.setName(request.getName());
        exercise.setDescription(request.getDescription());
        exercise.setExerciseCategory(exerciseCategory);
        exercise.setWeighted(request.getWeighted());

        return exerciseRepository.save(exercise);
    }

    public void delete(Long id){
        Exercise existing = findById(id);
        exerciseRepository.delete(existing);
    }
}
