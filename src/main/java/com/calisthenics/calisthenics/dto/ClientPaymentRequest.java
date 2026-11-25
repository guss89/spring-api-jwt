package com.calisthenics.calisthenics.dto;

import com.calisthenics.calisthenics.model.catalogs.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientPaymentRequest {

    private Long id;

    @NotNull
    @Positive
    private BigDecimal amount;

    private String paymentMethod;

    @NotNull
    private LocalDate paymentDate;

    private PaymentStatus status;
    private String reference;
    private String notes;
    private LocalDateTime createdAt;

    @NotNull
    private Long userId;

    @NotNull
    private Long clientMembershipId;

}
