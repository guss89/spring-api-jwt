package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.EvaluationRequest;
import com.calisthenics.calisthenics.dto.EvaluationResponse;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.Evaluation;
import com.calisthenics.calisthenics.model.Exercise;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.repository.CoachClientRepository;
import com.calisthenics.calisthenics.repository.EvaluationRepository;
import com.calisthenics.calisthenics.repository.ExerciseRepository;
import com.calisthenics.calisthenics.repository.UserRepository;
import com.calisthenics.calisthenics.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvaluationService {
    private final ExerciseRepository exerciseRepository;
    private final EvaluationRepository evaluationRepository;
    private final UserRepository userRepository;
    private final CoachClientRepository coachClientRepository;

    public EvaluationService(
            ExerciseRepository exerciseRepository,
            EvaluationRepository evaluationRepository,
            UserRepository userRepository,
            CoachClientRepository coachClientRepository
    ){
        this.exerciseRepository = exerciseRepository;
        this.evaluationRepository = evaluationRepository;
        this.userRepository = userRepository;
        this.coachClientRepository = coachClientRepository;
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

        return EvaluationResponse.from(evaluationRepository.save(evaluation));
    }

    public List<EvaluationResponse> getAll(Long userId, CurrentUser caller){
        List<Evaluation> evaluations;
        if (caller.admin()) {
            evaluations = (userId == null)
                    ? evaluationRepository.findAll()
                    : evaluationRepository.findByUserId(userId);
        } else if (caller.coach()) {
            if (userId != null) {
                requireAssignedClient(caller, userId);
                evaluations = evaluationRepository.findByUserId(userId);
            } else {
                List<Long> clientIds = coachClientRepository.findByCoachId(caller.id()).stream()
                        .map(cc -> cc.getClient().getId())
                        .toList();
                evaluations = clientIds.isEmpty()
                        ? List.of()
                        : evaluationRepository.findByUserIdIn(clientIds);
            }
        } else {
            if (userId != null) {
                caller.requireAccessTo(userId);
            }
            evaluations = evaluationRepository.findByUserId(caller.id());
        }
        return evaluations.stream().map(EvaluationResponse::from).toList();
    }

    public EvaluationResponse getById(Long id, CurrentUser caller){
        Evaluation evaluation = findById(id);
        Long ownerId = evaluation.getUser().getId();
        if (caller.coach() && !caller.admin()) {
            requireAssignedClient(caller, ownerId);
        } else {
            caller.requireAccessTo(ownerId);
        }
        return EvaluationResponse.from(evaluation);
    }

    private void requireAssignedClient(CurrentUser caller, Long clientId){
        if (!coachClientRepository.existsByCoachIdAndClientId(caller.id(), clientId)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    public Evaluation findById(Long id){
        return evaluationRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Evaluation not found"));
    }

    public EvaluationResponse update(Long id, EvaluationRequest request){
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
        return EvaluationResponse.from(evaluationRepository.save(evaluation));
    }

    public void delete(Long id){
        Evaluation existing = findById(id);
        evaluationRepository.delete(existing);
    }

}
