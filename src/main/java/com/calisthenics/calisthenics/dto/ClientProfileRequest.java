package com.calisthenics.calisthenics.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientProfileRequest {

    @NotNull
    private Long userId;

    @Past
    private LocalDate birthDate;

    @Positive
    @Digits(integer = 3, fraction = 2)
    private BigDecimal initialWeight;

    @Positive
    @Digits(integer = 3, fraction = 2)
    private BigDecimal initialHeight;
}
