package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.EvaluationRequest;
import com.calisthenics.calisthenics.dto.EvaluationResponse;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.Evaluation;
import com.calisthenics.calisthenics.model.Exercise;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.repository.ClientRepository;
import com.calisthenics.calisthenics.repository.EvaluationRepository;
import com.calisthenics.calisthenics.repository.ExerciseRepository;
import com.calisthenics.calisthenics.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvaluationService {
    private final ExerciseRepository exerciseRepository;
    private final EvaluationRepository evaluationRepository;
    private final UserRepository userRepository;

    public EvaluationService(
            ExerciseRepository exerciseRepository,
            EvaluationRepository evaluationRepository,
            UserRepository userRepository
    ){
        this.exerciseRepository = exerciseRepository;
        this.evaluationRepository = evaluationRepository;
        this.userRepository = userRepository;
    }

    public EvaluationResponse save(EvaluationRequest request){
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(()->new ResourceNotFoundException("User not found"));
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(()->new ResourceNotFoundException("Exercise not found"));

        Evaluation evaluation = new Evaluation();
        evaluation.setMaxReps(request.getMaxReps());
        evaluation.setMaxWeight(request.getMaxWeight());
        evaluation.setNotes(request.getNotes());
        evaluation.setRegisterDate(request.getRegisterDate());
        evaluation.setUser(user);
        evaluation.setExercise(exercise);

        Evaluation saved = evaluationRepository.save(evaluation);
        return new EvaluationResponse(
                saved.getId(),
                saved.getMaxReps(),
                saved.getMaxWeight(),
                saved.getNotes(),
                saved.getRegisterDate(),
                saved.getUser().getId(),
                saved.getExercise().getId()
        );
    }

    public List<Evaluation> getAll(){ return evaluationRepository.findAll(); }

    public Evaluation findById(Long id){
        return evaluationRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Evaluation not found"));
    }

    public Evaluation update(Long id, EvaluationRequest request){
        Evaluation evaluation = findById(id);
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(()->new ResourceNotFoundException("User not found"));
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(()->new ResourceNotFoundException("Exercise not found"));

        evaluation.setMaxReps(request.getMaxReps());
        evaluation.setMaxWeight(request.getMaxWeight());
        evaluation.setNotes(request.getNotes());
        evaluation.setRegisterDate(request.getRegisterDate());
        evaluation.setUser(user);
        evaluation.setExercise(exercise);
        return evaluation;
    }

    public void delete(Long id){
        Evaluation existing = findById(id);
        evaluationRepository.delete(existing);
    }

}
