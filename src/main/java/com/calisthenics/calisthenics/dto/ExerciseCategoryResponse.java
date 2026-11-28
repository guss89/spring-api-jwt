package com.calisthenics.calisthenics.dto;

import com.calisthenics.calisthenics.model.ExerciseCategory;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExerciseCategoryResponse {

    private Long id;
    private String name;

    public static ExerciseCategoryResponse from(ExerciseCategory entity) {
        return new ExerciseCategoryResponse(entity.getId(), entity.getName());
    }
}
