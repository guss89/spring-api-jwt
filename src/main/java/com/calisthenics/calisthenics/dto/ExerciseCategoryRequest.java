package com.calisthenics.calisthenics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseCategoryRequest {

    @NotBlank
    private String name;
}
