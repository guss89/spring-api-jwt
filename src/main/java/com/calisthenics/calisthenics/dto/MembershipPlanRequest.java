package com.calisthenics.calisthenics.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MembershipPlanRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    @Positive
    @Digits(integer = 6, fraction = 2)
    private BigDecimal price;

    @NotNull
    @Positive
    private Integer durationDays;
}
