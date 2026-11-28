package com.calisthenics.calisthenics.dto;

import com.calisthenics.calisthenics.model.TypeRoutine;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TypeRoutineResponse {

    private Long id;
    private String name;

    public static TypeRoutineResponse from(TypeRoutine entity) {
        return new TypeRoutineResponse(entity.getId(), entity.getName());
    }
}
