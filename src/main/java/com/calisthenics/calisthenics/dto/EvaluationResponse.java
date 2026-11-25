package com.calisthenics.calisthenics.dto;

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

}
