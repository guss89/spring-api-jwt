package com.calisthenics.calisthenics.repository;

import com.calisthenics.calisthenics.model.CoachClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoachClientRepository extends JpaRepository<CoachClient, Long> {

    boolean existsByCoachIdAndClientId(Long coachId, Long clientId);

    List<CoachClient> findByCoachId(Long coachId);

    List<CoachClient> findByClientId(Long clientId);
}
