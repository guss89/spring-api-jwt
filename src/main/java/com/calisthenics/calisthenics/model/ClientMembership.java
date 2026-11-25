package com.calisthenics.calisthenics.model;

import com.calisthenics.calisthenics.model.catalogs.MembershipStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "client_membership")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Relación con MembershipPlan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_plan_id", nullable = false)
    private MembershipPlan membershipPlan;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = MembershipStatus.ACTIVE;
        }
    }
}
