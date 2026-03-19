package com.calisthenics.calisthenics.dto;

import com.calisthenics.calisthenics.model.Exercise;
import com.calisthenics.calisthenics.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EvaluationResponse {
    public Long id;
    public Integer maxReps;
    public BigDecimal maxWeight;
    public String notes;
    public LocalDate registerDate;
    public Long exerciseId;
    public Long userId;

    public EvaluationResponse(
            Long id,
            Integer maxReps,
            BigDecimal maxWeight,
            String notes,
            LocalDate registerDate,
            User user,
            Exercise exercise) {
        this.id = id;
        this.maxReps = maxReps;
        this.maxWeight = maxWeight;
        this.notes = notes;
        this.registerDate = registerDate;
    }

    public void setId(Long id) { this.id = id; }
    public void setMaxReps(int maxReps) { this.maxReps = maxReps; }
    public void setMaxWeight(BigDecimal maxWeight) { this.maxWeight = maxWeight; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setRegisterDate(LocalDate registerDate) { this.registerDate = registerDate; }
    public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getId() { return id;}
    public Integer getMaxReps() { return maxReps; }
    public BigDecimal getMaxWeight() { return maxWeight; }
    public String getNotes() { return notes; }
    public LocalDate getRegisterDate() { return registerDate; }
    public Long getExerciseId() { return exerciseId; }
    public Long getClientId() { return userId; }
}
