package com.calisthenics.calisthenics.dto;

public class RoutineExerciseResponse {

    public Long id;
    public int reps;
    public int restSeconds;
    public int sets;
    public Long bodyRegionId;
    public Long exerciseId;
    public Long routineId;

    public RoutineExerciseResponse(
            Long id,
            int reps,
            int restSeconds,
            int sets,
            Long bodyRegionId,
            Long exerciseId,
            Long routineId) {
        this.id = id;
        this.reps = reps;
        this.restSeconds = restSeconds;
        this.sets = sets;
        this.bodyRegionId = bodyRegionId;
        this.exerciseId = exerciseId;
        this.routineId = routineId;
    }

    public Long getId() {
        return id;
    }

    public int getReps() { return reps; }

    public int getRestSeconds() { return restSeconds; }

    public int getSets() { return sets; }

    public Long getRoutineId() { return routineId; }

    public Long getBodyRegionId() { return bodyRegionId; }

    public Long getExerciseId() { return exerciseId;}

    public void setId(Long id) { this.id = id; }

    public void setReps(int reps) { this.reps = reps;}

    public void setRestSeconds(int restSeconds) { this.restSeconds = restSeconds; }

    public void setSets(int sets) { this.sets = sets; }

    public void setBodyRegionId(Long bodyRegionId) { this.bodyRegionId = bodyRegionId; }

    public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }

    public void setRoutineId(Long routineId) { this.routineId = routineId; }
}
