package com.calisthenics.calisthenics.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exercise_category")
public class ExerciseCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(
            mappedBy = "exerciseCategory",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )

    public List<Exercise> exercises = new ArrayList<>();

    /*Getters*/
    public Long getId(){
        return  id;
    }
    public String getName(){
        return name;
    }
    public List<Exercise> getExercises(){
        return  exercises;
    }
    /*Setters*/
    public void  setName(String name){
        this.name = name;
    }
}
