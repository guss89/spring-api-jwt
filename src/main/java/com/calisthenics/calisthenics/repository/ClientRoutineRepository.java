package com.calisthenics.calisthenics.repository;

import com.calisthenics.calisthenics.model.ClientRoutine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRoutineRepository extends JpaRepository<ClientRoutine, Long> {
    List<ClientRoutine> findByUserId(Long userId);
}
