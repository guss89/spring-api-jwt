package com.calisthenics.calisthenics.repository;

import com.calisthenics.calisthenics.model.ClientProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<ClientProfile, Long> {
    boolean existsByUserId(Long userId);
    Optional<ClientProfile> findByUserId(Long userId);
}
