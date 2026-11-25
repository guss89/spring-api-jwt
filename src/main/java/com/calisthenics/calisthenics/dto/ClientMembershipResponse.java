package com.calisthenics.calisthenics.dto;

import com.calisthenics.calisthenics.model.catalogs.MembershipStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientMembershipResponse {

    private Long id;
    private Long userId;
    private Long membershipPlanId;
    private LocalDate startDate;
    private LocalDate endDate;
    private MembershipStatus status;
    private LocalDateTime createdAt;
}
