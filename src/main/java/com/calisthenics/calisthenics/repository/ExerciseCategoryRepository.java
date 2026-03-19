package com.calisthenics.calisthenics.repository;

import com.calisthenics.calisthenics.model.ExerciseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseCategoryRepository extends JpaRepository<ExerciseCategory, Long> {
}
