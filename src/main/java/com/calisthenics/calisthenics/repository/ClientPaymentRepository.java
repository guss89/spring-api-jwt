package com.calisthenics.calisthenics.repository;

import com.calisthenics.calisthenics.model.ClientPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientPaymentRepository extends JpaRepository<ClientPayment, Long> {
}
