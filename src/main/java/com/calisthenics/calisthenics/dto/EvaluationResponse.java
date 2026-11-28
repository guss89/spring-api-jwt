package com.calisthenics.calisthenics.dto;

import com.calisthenics.calisthenics.model.Evaluation;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EvaluationResponse {

    private Long id;
    private Long maxReps;
    private BigDecimal maxWeight;
    private String notes;
    private LocalDate registerDate;
    private Long exerciseId;
    private Long userId;

    public static EvaluationResponse from(Evaluation evaluation) {
        return new EvaluationResponse(evaluation.getId(), evaluation.getMaxReps(), evaluation.getMaxWeight(),
                evaluation.getNotes(), evaluation.getRegisterDate(), evaluation.getExercise().getId(),
                evaluation.getUser().getId());
    }
}
