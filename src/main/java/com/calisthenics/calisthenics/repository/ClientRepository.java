package com.calisthenics.calisthenics.repository;

import com.calisthenics.calisthenics.model.ClientProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<ClientProfile, Long> {
}
