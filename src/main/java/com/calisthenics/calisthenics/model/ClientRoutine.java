package com.calisthenics.calisthenics.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "client_routine")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientRoutine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @Column(nullable = false)
    private Boolean active;

    // Relación con User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Relación con Routine
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @PrePersist
    public void prePersist() {
        if (this.active == null) {
            this.active = true;
        }
    }
}
