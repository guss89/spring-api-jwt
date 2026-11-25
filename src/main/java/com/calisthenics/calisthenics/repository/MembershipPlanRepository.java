package com.calisthenics.calisthenics.repository;

import com.calisthenics.calisthenics.model.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {
}
