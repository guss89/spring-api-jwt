package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.CoachClientRequest;
import com.calisthenics.calisthenics.dto.CoachClientResponse;
import com.calisthenics.calisthenics.exception.ConflictException;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.CoachClient;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.model.catalogs.Role;
import com.calisthenics.calisthenics.repository.ClientRepository;
import com.calisthenics.calisthenics.repository.CoachClientRepository;
import com.calisthenics.calisthenics.repository.UserRepository;
import com.calisthenics.calisthenics.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoachClientService {
    private final CoachClientRepository coachClientRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    public CoachClientService(CoachClientRepository coachClientRepository,
                              UserRepository userRepository,
                              ClientRepository clientRepository) {
        this.coachClientRepository = coachClientRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
    }

    public CoachClientResponse assignCoachToClient(CoachClientRequest request, CurrentUser caller) {
        requireAdmin(caller);

        User coach = findUser(request.getCoachId(), "Coach not found");
        User client = findUser(request.getClientId(), "Client not found");

        if (coach.getRole() != Role.COACH) {
            throw new IllegalArgumentException("El usuario no tiene rol COACH");
        }
        if (client.getRole() != Role.CLIENT) {
            throw new IllegalArgumentException("El usuario no tiene rol CLIENT");
        }
        if (!clientRepository.existsByUserId(client.getId())) {
            throw new ResourceNotFoundException("El cliente no tiene perfil registrado");
        }
        if (coachClientRepository.existsByCoachIdAndClientId(coach.getId(), client.getId())) {
            throw new ConflictException("Esta relación coach-cliente ya existe");
        }

        CoachClient relation = CoachClient.builder()
                .coach(coach)
                .client(client)
                .build();

        return CoachClientResponse.from(coachClientRepository.save(relation));
    }

    public List<CoachClientResponse> getClientsByCoach(Long coachId, CurrentUser caller) {
        if (!caller.admin() && !(caller.coach() && caller.isOwner(coachId))) {
            throw new AccessDeniedException("Access denied");
        }
        findUser(coachId, "Coach not found");
        return coachClientRepository.findByCoachId(coachId).stream()
                .map(CoachClientResponse::from).toList();
    }

    public List<CoachClientResponse> getCoachesByClient(Long clientId, CurrentUser caller) {
        if (!caller.admin() && !(caller.client() && caller.isOwner(clientId))) {
            throw new AccessDeniedException("Access denied");
        }
        findUser(clientId, "Client not found");
        return coachClientRepository.findByClientId(clientId).stream()
                .map(CoachClientResponse::from).toList();
    }

    public void delete(Long id, CurrentUser caller) {
        requireAdmin(caller);
        CoachClient relation = coachClientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coach-client relation not found"));
        coachClientRepository.delete(relation);
    }

    private void requireAdmin(CurrentUser caller) {
        if (!caller.admin()) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private User findUser(Long id, String notFoundMessage) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(notFoundMessage));
    }
}
