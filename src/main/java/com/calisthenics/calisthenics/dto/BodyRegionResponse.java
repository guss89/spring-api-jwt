package com.calisthenics.calisthenics.dto;

import com.calisthenics.calisthenics.model.BodyRegion;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BodyRegionResponse {

    private Long id;
    private String name;

    public static BodyRegionResponse from(BodyRegion entity) {
        return new BodyRegionResponse(entity.getId(), entity.getName());
    }
}
