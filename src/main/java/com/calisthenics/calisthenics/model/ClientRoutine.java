package com.calisthenics.calisthenics.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "client_routine")
public class ClientRoutine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String observation;
    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    /*Getters*/
    public Long getId() {return id;}
    public LocalDate getStartDate(){ return startDate; }
    public LocalDate getEndDate(){ return startDate; }
    public String getObservation(){ return observation; }
    public Boolean getActive(){ return  active; }
    public User getUser() {
        return user;
    }
    public Routine getRoutine(){
        return routine;
    }

    /*Setters*/
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    public void setObservation(String observation) {
        this.observation = observation;
    }
    public void setUser(User user) { this.user = user; }
    public void setRoutine(Routine routine) { this.routine = routine; }
    public void setActive(Boolean active) { this.active = active; }

}
