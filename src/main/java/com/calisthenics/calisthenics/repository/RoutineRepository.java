package com.calisthenics.calisthenics.repository;

import com.calisthenics.calisthenics.model.Routine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutineRepository extends JpaRepository<Routine, Long> {
}
