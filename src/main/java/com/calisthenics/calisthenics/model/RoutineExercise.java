package com.calisthenics.calisthenics.model;

import jakarta.persistence.*;

@Entity
@Table(name = "routine_exercise")
public class RoutineExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private int sets;
    private int reps;
    private int restSeconds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "body_region_id", nullable = false)
    private BodyRegion bodyRegion;

    /*Getters*/
    public Long getId(){ return id; }
    public int getSets(){ return sets;}
    public int getReps(){ return reps;}
    public int getRestSeconds(){ return restSeconds;}

    public Routine getRoutine() { return routine; }
    public Exercise getExercise() { return exercise; }
    public BodyRegion getBodyRegion() { return bodyRegion; }

    /*Setter*/

    public void setSets( int sets) { this.sets = sets; }
    public void setReps( int reps) { this.reps = reps; }
    public void setRestSeconds( int restSeconds) { this.restSeconds = restSeconds; }

    public void setRoutine(Routine routine){ this.routine = routine; }
    public void setExercise(Exercise exercise){ this.exercise = exercise; }
    public void setBodyRegion(BodyRegion bodyRegion){ this.bodyRegion = bodyRegion; }
}
