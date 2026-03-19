package com.calisthenics.calisthenics.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "coach_client",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"coach_id", "client_id"})
        }
)
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

    public CoachClient() {}

    public CoachClient(User coach, User client) {
        this.coach = coach;
        this.client = client;
    }

    public Long getId() {
        return id;
    }

    public User getCoach() {
        return coach;
    }

    public User getClient() {
        return client;
    }

    public void setCoach(User coach) {
        this.coach = coach;
    }

    public void setClient(User client) {
        this.client = client;
    }
}