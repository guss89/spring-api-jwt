package com.calisthenics.calisthenics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.calisthenics.calisthenics.model.catalogs.PaymentStatus;
import com.calisthenics.calisthenics.model.catalogs.Status;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientPaymentResponse {

    private Long id;
    private BigDecimal amount;
    private String paymentMethod;
    private LocalDate paymentDate;
    private PaymentStatus status;
    private String reference;
    private String notes;
    private LocalDateTime createdAt;
    private Long userId;
    private Long clientMembershipId;

}
