package com.calisthenics.calisthenics.dto;

import com.calisthenics.calisthenics.model.Exercise;

public class ExerciseResponse {
    public Long id;
    public Long exerciseCategoryId;
    public String name;
    public String description;
    public  boolean isWeighted;

    public ExerciseResponse(
            Long id,
            String name,
            String description,
            Long exerciseCategoryId,
            boolean isWeighted
    ){
        this.id = id;
        this.name = name;
        this.description = description;
        this.exerciseCategoryId = exerciseCategoryId;
        this.isWeighted = isWeighted;
    }

    public static ExerciseResponse from(Exercise exercise) {
        return new ExerciseResponse(
                exercise.getId(),
                exercise.getName(),
                exercise.getDescription(),
                exercise.getExerciseCategory().getId(),
                Boolean.TRUE.equals(exercise.getIsWeighted())
        );
    }

    public Long getExerciseCategoryId(){
        return exerciseCategoryId;
    }
    public String getName(){
        return this.name;
    }
    public String getDescription(){
        return this.description;
    }
    public boolean isWeighted() {
        return isWeighted;
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
        isWeighted = weighted;
    }
}
