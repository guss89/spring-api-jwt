package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.model.CoachClient;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.model.catalogs.Role;
import com.calisthenics.calisthenics.repository.CoachClientRepository;
import com.calisthenics.calisthenics.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoachClientService {
    private final CoachClientRepository coachClientRepository;
    private final UserRepository userRepository;

    public CoachClientService(CoachClientRepository coachClientRepository,
                              UserRepository userRepository) {
        this.coachClientRepository = coachClientRepository;
        this.userRepository = userRepository;
    }

    public CoachClient assignCoachToClient(Long coachId, Long clientId) {

        if (coachId.equals(clientId)) {
            throw new RuntimeException("Coach y Client no pueden ser el mismo usuario");
        }

        User coach = userRepository.findById(coachId)
                .orElseThrow(() -> new RuntimeException("Coach no encontrado"));

        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client no encontrado"));

        if (coach.getRole() != Role.COACH) {
            throw new RuntimeException("El usuario no tiene rol COACH");
        }

        if (client.getRole() != Role.CLIENT) {
            throw new RuntimeException("El usuario no tiene rol CLIENT");
        }

        boolean alreadyExists = coachClientRepository
                .existsByCoachIdAndClientId(coachId, clientId);

        if (alreadyExists) {
            throw new RuntimeException("Esta relación ya existe");
        }

        CoachClient relation = CoachClient.builder()
                .coach(coach)
                .client(client)
                .build();

        return coachClientRepository.save(relation);
    }

    public List<CoachClient> getClientsByCoach(Long coachId) {
        return coachClientRepository.findByCoachId(coachId);
    }

    public List<CoachClient> getCoachesByClient(Long clientId) {
        return coachClientRepository.findByClientId(clientId);
    }
}
