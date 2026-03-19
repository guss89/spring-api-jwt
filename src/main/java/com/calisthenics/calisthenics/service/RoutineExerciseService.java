package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.RoutineExerciseRequest;
import com.calisthenics.calisthenics.dto.RoutineExerciseResponse;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.BodyRegion;
import com.calisthenics.calisthenics.model.Exercise;
import com.calisthenics.calisthenics.model.Routine;
import com.calisthenics.calisthenics.model.RoutineExercise;
import com.calisthenics.calisthenics.repository.BodyRegionRepository;
import com.calisthenics.calisthenics.repository.ExerciseRepository;
import com.calisthenics.calisthenics.repository.RoutineExerciseRepository;
import com.calisthenics.calisthenics.repository.RoutineRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoutineExerciseService {

    private final RoutineRepository routineRepository;
    private final ExerciseRepository exerciseRepository;
    private final BodyRegionRepository bodyRegionRepository;
    private final RoutineExerciseRepository routineExerciseRepository;

    public RoutineExerciseService (
            RoutineRepository routineRepository,
            ExerciseRepository exerciseRepository,
            BodyRegionRepository bodyRegionRepository,
            RoutineExerciseRepository routineExerciseRepository){
        this.routineRepository = routineRepository;
        this.exerciseRepository = exerciseRepository;
        this.bodyRegionRepository = bodyRegionRepository;
        this.routineExerciseRepository = routineExerciseRepository;
    }

    public RoutineExerciseResponse save(RoutineExerciseRequest request){
        Routine routine = routineRepository.findById(request.getRoutineId())
                .orElseThrow(()->new ResourceNotFoundException("Routine not found"));

        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(()->new ResourceNotFoundException("Exercise not found"));

        BodyRegion bodyRegion =  bodyRegionRepository.findById(request.getBodyRegionId())
                .orElseThrow(()->new ResourceNotFoundException("Body Region not found"));

        RoutineExercise routineExercise = new RoutineExercise();
        routineExercise.setReps(request.getReps());
        routineExercise.setRestSeconds(request.getRestSeconds());
        routineExercise.setSets(request.getSets());
        routineExercise.setBodyRegion(bodyRegion);
        routineExercise.setExercise(exercise);
        routineExercise.setRoutine(routine);

        RoutineExercise saved = routineExerciseRepository.save(routineExercise);

        return new RoutineExerciseResponse(
                saved.getId(),
                saved.getReps(),
                saved.getRestSeconds(),
                saved.getSets(),
                saved.getBodyRegion().getId(),
                saved.getExercise().getId(),
                saved.getRoutine().getId()
        );
    }

    public List<RoutineExercise> getAll() { return routineExerciseRepository.findAll(); }

    public RoutineExercise findById(Long id){
        return routineExerciseRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Routine exercise not found"));
    }

    public RoutineExercise update(Long id, RoutineExerciseRequest routineExerciseRequest){
        RoutineExercise routineExercise =  findById(id);

        BodyRegion bodyRegion = bodyRegionRepository.findById(routineExerciseRequest.getBodyRegionId())
                .orElseThrow(()->new ResourceNotFoundException("Body Region exercise not found"));
        Exercise exercise = exerciseRepository.findById(routineExerciseRequest.getExerciseId())
                .orElseThrow(()->new ResourceNotFoundException("Exercise not found"));
        Routine routine = routineRepository.findById(routineExerciseRequest.getRoutineId())
                .orElseThrow(()->new ResourceNotFoundException("Routine exercise not found"));

        routineExercise.setReps(routineExerciseRequest.getReps());
        routineExercise.setRestSeconds(routineExerciseRequest.getRestSeconds());
        routineExercise.setSets(routineExerciseRequest.getSets());
        routineExercise.setBodyRegion(bodyRegion);
        routineExercise.setRoutine(routine);
        routineExercise.setExercise(exercise);

        return routineExerciseRepository.save(routineExercise);
    }

    public void delete(Long id){
        RoutineExercise existing = findById(id);
        routineExerciseRepository.delete(existing);
    }
}
