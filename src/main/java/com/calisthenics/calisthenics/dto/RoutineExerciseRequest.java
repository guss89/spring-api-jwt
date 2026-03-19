package com.calisthenics.calisthenics.dto;

public class RoutineExerciseRequest {

    public int reps;
    public int restSeconds;
    public int sets;
    public Long bodyRegionId;
    public Long exerciseId;
    public Long routineId;

    public  RoutineExerciseRequest(){}

    public int getReps() { return reps; }

    public int getRestSeconds() { return restSeconds; }

    public int getSets() { return sets; }

    public Long getRoutineId() { return routineId; }

    public Long getBodyRegionId() { return bodyRegionId; }

    public Long getExerciseId() { return exerciseId;}

    public void setReps(int reps) { this.reps = reps;}

    public void setRestSeconds(int restSeconds) { this.restSeconds = restSeconds; }

    public void setSets(int sets) { this.sets = sets; }

    public void setBodyRegionId(Long bodyRegionId) { this.bodyRegionId = bodyRegionId; }

    public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }

    public void setRoutineId(Long routineId) { this.routineId = routineId; }
}
