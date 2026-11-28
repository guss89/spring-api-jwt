package com.calisthenics.calisthenics.dto;

import com.calisthenics.calisthenics.model.CoachClient;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CoachClientResponse {

    private Long id;
    private Long coachId;
    private Long clientId;

    public static CoachClientResponse from(CoachClient relation) {
        return new CoachClientResponse(relation.getId(), relation.getCoach().getId(),
                relation.getClient().getId());
    }
}
