package com.calisthenics.calisthenics.repository;

import com.calisthenics.calisthenics.model.ClientPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientPaymentRepository extends JpaRepository<ClientPayment, Long> {
    List<ClientPayment> findByUserId(Long userId);

    boolean existsByClientMembershipId(Long clientMembershipId);
}
