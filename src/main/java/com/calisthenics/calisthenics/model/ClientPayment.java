package com.calisthenics.calisthenics.model;

import com.calisthenics.calisthenics.model.catalogs.PaymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.*;

@Entity
@Table(name = "client_payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con User (cliente)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Relación con ClientMembership (opcional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_membership_id")
    private ClientMembership clientMembership;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(length = 100)
    private String reference;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = PaymentStatus.PAID;
        }
    }
}
