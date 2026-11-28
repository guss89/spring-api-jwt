package com.calisthenics.calisthenics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TypeRoutineRequest {

    @NotBlank
    private String name;
}
