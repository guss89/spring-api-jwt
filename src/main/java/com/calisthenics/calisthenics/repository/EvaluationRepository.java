package com.calisthenics.calisthenics.repository;

import com.calisthenics.calisthenics.model.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByUserId(Long userId);

    List<Evaluation> findByUserIdIn(Collection<Long> userIds);
}
