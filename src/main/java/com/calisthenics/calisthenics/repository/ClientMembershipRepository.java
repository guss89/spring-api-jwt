package com.calisthenics.calisthenics.repository;

import com.calisthenics.calisthenics.model.ClientMembership;
import com.calisthenics.calisthenics.model.catalogs.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientMembershipRepository extends JpaRepository<ClientMembership, Long> {
    List<ClientMembership> findByUserId(Long userId);

    boolean existsByMembershipPlanId(Long membershipPlanId);

    boolean existsByUserIdAndStatus(Long userId, MembershipStatus status);

    boolean existsByUserIdAndStatusAndIdNot(Long userId, MembershipStatus status, Long id);
}
