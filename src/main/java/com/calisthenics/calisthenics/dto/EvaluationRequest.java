package com.calisthenics.calisthenics.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EvaluationRequest {

    public Long maxReps;
    public BigDecimal maxWeight;
    public String notes;
    @NotNull
    public LocalDate registerDate;
    @NotNull
    public Long exerciseId;
    @NotNull
    public Long userId;

    public void setMaxReps(Long maxReps) { this.maxReps = maxReps; }
    public void setMaxWeight(BigDecimal maxWeight) { this.maxWeight = maxWeight; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setRegisterDate(LocalDate registerDate) { this.registerDate = registerDate; }
    public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }
    public void setUserId(Long clientId) { this.userId = clientId; }

    public Long getMaxReps() { return maxReps; }
    public BigDecimal getMaxWeight() { return maxWeight; }
    public String getNotes() { return notes; }
    public LocalDate getRegisterDate() { return registerDate; }
    public Long getExerciseId() { return exerciseId; }
    public Long getUserId() { return userId;}
}
