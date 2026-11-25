package com.calisthenics.calisthenics.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "membership_plan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Relación inversa (opcional pero recomendable)
    @OneToMany(mappedBy = "membershipPlan")
    private List<ClientMembership> clientMemberships;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

}
