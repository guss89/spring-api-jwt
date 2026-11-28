package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.ClientRoutineRequest;
import com.calisthenics.calisthenics.dto.ClientRoutineResponse;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.ClientRoutine;
import com.calisthenics.calisthenics.model.Routine;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.model.catalogs.Role;
import com.calisthenics.calisthenics.repository.ClientRepository;
import com.calisthenics.calisthenics.repository.ClientRoutineRepository;
import com.calisthenics.calisthenics.repository.RoutineRepository;
import com.calisthenics.calisthenics.repository.UserRepository;
import com.calisthenics.calisthenics.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientRoutineService {
    private final UserRepository userRepository;
    private final RoutineRepository routineRepository;
    private final ClientRoutineRepository clientRoutineRepository;
    private final ClientRepository clientRepository;

    public ClientRoutineService(
            UserRepository userRepository,
            RoutineRepository routineRepository,
            ClientRoutineRepository clientRoutineRepository,
            ClientRepository clientRepository
    ){
        this.clientRepository = clientRepository;
        this.routineRepository = routineRepository;
        this.userRepository = userRepository;
        this.clientRoutineRepository = clientRoutineRepository;
    }

    public ClientRoutineResponse save(ClientRoutineRequest request){
        User user = findClientUser(request.getUserId());

        Routine routine = routineRepository.findById(request.getRoutineId())
                .orElseThrow(()->new ResourceNotFoundException("Routine not found"));

        ClientRoutine clientRoutine = new ClientRoutine();
        clientRoutine.setUser(user);
        clientRoutine.setRoutine(routine);
        clientRoutine.setStartDate(request.getStartDate());
        clientRoutine.setEndDate(request.getEndDate());
        clientRoutine.setObservations(request.getObservations());
        clientRoutine.setActive(request.getActive());

        return ClientRoutineResponse.from(clientRoutineRepository.save(clientRoutine));
    }

    public List<ClientRoutineResponse> getAll(CurrentUser caller) {
        List<ClientRoutine> routines = caller.admin()
                ? clientRoutineRepository.findAll()
                : clientRoutineRepository.findByUserId(caller.id());
        return routines.stream().map(ClientRoutineResponse::from).toList();
    }

    public ClientRoutineResponse getById(Long id, CurrentUser caller) {
        ClientRoutine clientRoutine = findById(id);
        if (!caller.coach()) {
            caller.requireAccessTo(clientRoutine.getUser().getId());
        }
        return ClientRoutineResponse.from(clientRoutine);
    }

    public ClientRoutine findById(Long id){
        return clientRoutineRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Client Routine not found"));
    }

    public ClientRoutineResponse update(Long id, ClientRoutineRequest request){
        ClientRoutine clientRoutine =  findById(id);
        User user = findClientUser(request.getUserId());
        if (!user.getId().equals(clientRoutine.getUser().getId())) {
            throw new IllegalArgumentException("No se puede cambiar el cliente de una asignación existente");
        }
        Routine routine = routineRepository.findById(request.getRoutineId())
                .orElseThrow(()->new ResourceNotFoundException("Routine not found"));

        clientRoutine.setRoutine(routine);
        clientRoutine.setStartDate(request.getStartDate());
        clientRoutine.setEndDate(request.getEndDate());
        clientRoutine.setObservations(request.getObservations());
        clientRoutine.setActive(request.getActive());

        return ClientRoutineResponse.from(clientRoutineRepository.save(clientRoutine));
    }

    private User findClientUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new ResourceNotFoundException("User not found"));
        if (user.getRole() != Role.CLIENT) {
            throw new IllegalArgumentException("La asignación solo puede hacerse a un usuario con rol CLIENT");
        }
        if (!clientRepository.existsByUserId(userId)) {
            throw new ResourceNotFoundException("El cliente no tiene perfil registrado");
        }
        return user;
    }

    public void delete(Long id){
        ClientRoutine existing = findById(id);
        clientRoutineRepository.delete(existing);
    }
}
