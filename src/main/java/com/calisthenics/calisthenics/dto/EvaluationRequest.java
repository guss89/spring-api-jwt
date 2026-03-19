package com.calisthenics.calisthenics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EvaluationRequest {

    public Integer maxReps;
    public BigDecimal maxWeight;
    public String notes;
    public LocalDate registerDate;
    public Long exerciseId;
    public Long userId;

    public void setMaxReps(Integer maxReps) { this.maxReps = maxReps; }
    public void setMaxWeight(BigDecimal maxWeight) { this.maxWeight = maxWeight; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setRegisterDate(LocalDate registerDate) { this.registerDate = registerDate; }
    public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }
    public void setUserId(Long clientId) { this.userId = clientId; }

    public Integer getMaxReps() { return maxReps; }
    public BigDecimal getMaxWeight() { return maxWeight; }
    public String getNotes() { return notes; }
    public LocalDate getRegisterDate() { return registerDate; }
    public Long getExerciseId() { return exerciseId; }
    public Long getUserId() { return userId;}
}
