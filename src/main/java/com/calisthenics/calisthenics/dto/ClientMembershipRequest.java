package com.calisthenics.calisthenics.dto;

import com.calisthenics.calisthenics.model.catalogs.MembershipStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientMembershipRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long membershipPlanId;

    @NotNull
    private LocalDate startDate;

    private MembershipStatus status;
}
