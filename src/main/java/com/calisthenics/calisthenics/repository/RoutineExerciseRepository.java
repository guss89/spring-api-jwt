package com.calisthenics.calisthenics.repository;

import com.calisthenics.calisthenics.model.RoutineExercise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutineExerciseRepository extends JpaRepository<RoutineExercise, Long> {
}
