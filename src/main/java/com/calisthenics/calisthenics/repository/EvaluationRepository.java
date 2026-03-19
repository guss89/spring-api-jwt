package com.calisthenics.calisthenics.repository;

import com.calisthenics.calisthenics.model.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
}
