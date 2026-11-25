package com.calisthenics.calisthenics.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MembershipPlanResponse {

    private Long id;
    private String name;
    private BigDecimal price;
    private Integer durationDays;
    private LocalDateTime createdAt;
}
