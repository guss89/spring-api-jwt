package com.calisthenics.calisthenics.dto;

public class ExerciseRequest {
    public Long exerciseCategoryId;
    public String name;
    public String description;
    public boolean weighted;

    public ExerciseRequest() {
    }

    public Long getExerciseCategoryId(){ return exerciseCategoryId;}
    public String getName(){
        return this.name;
    }
    public String getDescription(){
        return this.description;
    }
    public boolean getWeighted() {
        return weighted;
    }

    public void setExerciseCategoryId(Long exerciseCategoryId) {
        this.exerciseCategoryId = exerciseCategoryId;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setWeighted(boolean weighted) {
        this.weighted = weighted;
    }
}
