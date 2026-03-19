package com.calisthenics.calisthenics.model;

import jakarta.persistence.*;

@Entity
@Table(name = "exercise")
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Boolean weighted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_category_id", nullable = false)
    private ExerciseCategory exerciseCategory;

    /*Getters*/
    public  Long getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public String getDescription(){
        return description;
    }
    public  Boolean getWeighted(){
        return weighted;
    }
    public ExerciseCategory getExerciseCategory() {
        return exerciseCategory;
    }

    /*Setters*/
    public void setName(String name){
        this.name = name;
    }
    public void setWeighted(boolean isWeighted){
        this.weighted = isWeighted;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public void setExerciseCategory(ExerciseCategory exerciseCategory) { this.exerciseCategory = exerciseCategory; }
}
