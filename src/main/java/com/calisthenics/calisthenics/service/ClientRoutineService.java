package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.ClientRoutineRequest;
import com.calisthenics.calisthenics.dto.ClientRoutineResponse;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.ClientRoutine;
import com.calisthenics.calisthenics.model.Routine;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.repository.ClientRoutineRepository;
import com.calisthenics.calisthenics.repository.RoutineRepository;
import com.calisthenics.calisthenics.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientRoutineService {
    private final UserRepository userRepository;
    private final RoutineRepository routineRepository;
    private final ClientRoutineRepository clientRoutineRepository;

    public ClientRoutineService(
            UserRepository userRepository,
            RoutineRepository routineRepository,
            ClientRoutineRepository clientRoutineRepository
    ){
        this.routineRepository = routineRepository;
        this.userRepository = userRepository;
        this.clientRoutineRepository = clientRoutineRepository;
    }

    public ClientRoutineResponse save(ClientRoutineRequest request){
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(()->new ResourceNotFoundException("User not found"));

        Routine routine = routineRepository.findById(request.getRoutineId())
                .orElseThrow(()->new ResourceNotFoundException("Routine not found"));

        ClientRoutine clientRoutine = new ClientRoutine();
        clientRoutine.setUser(user);
        clientRoutine.setRoutine(routine);
        clientRoutine.setStartDate(request.getStartDate());
        clientRoutine.setEndDate(request.getEndDate());
        clientRoutine.setObservation(request.getObservations());
        clientRoutine.setActive(request.getActive());

        ClientRoutine saved = clientRoutineRepository.save(clientRoutine);

        return new ClientRoutineResponse(
                saved.getId(),
                saved.getUser().getId(),
                saved.getRoutine().getId(),
                saved.getStartDate(),
                saved.getEndDate(),
                saved.getObservation(),
                saved.getActive()
        );
    }

    public List<ClientRoutine> getAll() { return clientRoutineRepository.findAll(); }

    public ClientRoutine findById(Long id){
        return clientRoutineRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Client Routine not found"));
    }

    public ClientRoutine update(Long id, ClientRoutineRequest request){
        ClientRoutine clientRoutine =  findById(id);
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(()->new ResourceNotFoundException("User not found"));
        Routine routine = routineRepository.findById(request.getRoutineId())
                .orElseThrow(()->new ResourceNotFoundException("Routine not found"));

        clientRoutine.setUser(user);
        clientRoutine.setRoutine(routine);
        clientRoutine.setStartDate(request.getStartDate());
        clientRoutine.setEndDate(request.getEndDate());
        clientRoutine.setObservation(request.getObservations());
        clientRoutine.setActive(request.getActive());

        return clientRoutineRepository.save(clientRoutine);
    }

    public void delete(Long id){
        ClientRoutine existing = findById(id);
        clientRoutineRepository.delete(existing);
    }
}
