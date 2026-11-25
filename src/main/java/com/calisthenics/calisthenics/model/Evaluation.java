package com.calisthenics.calisthenics.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "evaluation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "max_reps")
    private Long maxReps;

    @Column(name = "max_weight", precision = 6, scale = 2)
    private BigDecimal maxWeight;

    @Column(name = "register_date", nullable = false)
    private LocalDate registerDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Relación con User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Relación con Exercise
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;
}
