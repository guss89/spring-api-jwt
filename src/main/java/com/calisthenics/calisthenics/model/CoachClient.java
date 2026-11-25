package com.calisthenics.calisthenics.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "coach_client",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"coach_id", "client_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoachClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Coach (User con rol COACH)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private User coach;

    // Client (User con rol CLIENT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;
}