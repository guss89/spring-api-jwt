package com.calisthenics.calisthenics.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoachClientRequest {

    @NotNull
    private Long coachId;

    @NotNull
    private Long clientId;
}
