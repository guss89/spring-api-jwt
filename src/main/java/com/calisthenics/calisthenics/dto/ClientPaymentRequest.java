package com.calisthenics.calisthenics.dto;

import com.calisthenics.calisthenics.model.catalogs.Status;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ClientPaymentRequest {
    public Long id;
    public BigDecimal amount;
    public LocalDate paymentDate;
    public Status status;
    public Long userId;


    public void setId(Long id) {
        this.id = id;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Status setStatus() {
        return status;
    }

    public void setClientId(Long userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public Status getStatus() {
        return status;
    }

    public Long getClientId() {
        return userId;
    }
}
