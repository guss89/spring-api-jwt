package com.calisthenics.calisthenics.repository;

import com.calisthenics.calisthenics.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
}
