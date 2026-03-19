package com.calisthenics.calisthenics.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "evaluation")
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private Integer maxReps;
    private BigDecimal maxWeight;
    private LocalDate registerDate;
    private String notes;

    //
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    //
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    /*Getters*/
    public Long getId(){ return id; }
    public Integer getMaxReps(){ return maxReps; }
    public BigDecimal getMaxWeight(){ return maxWeight; }
    public LocalDate getRegisterDate(){ return registerDate; }
    public String getNotes(){ return notes; }
    public User getUser(){return user;}
    public Exercise getExerciseId(){return exercise;}

    /*Setters*/
    public void setMaxReps(Integer maxReps){ this.maxReps = maxReps; }
    public void setMaxWeight(BigDecimal weight){ maxWeight = weight; }
    public void setRegisterDate(LocalDate register){ registerDate = register; }
    public void setNotes(String notes){ this. notes = notes; }

    public void setUser(User user){ this.user = user; }
    public void setExerciseId(Exercise exercise){ this.exercise = exercise; }
}
