package com.calisthenics.calisthenics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.calisthenics.calisthenics.model.catalogs.Status;

public class ClientPaymentResponse {
    public Long id;
    public BigDecimal amount;
    public LocalDate paymentDate;
    public Status status;
    public Long clientId;

    public ClientPaymentResponse(
            Long id, BigDecimal amount, LocalDate paymentDate, Status status, Long clientId
    ){
        this.id = id;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.status = status;
        this.clientId = clientId;
    }

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

    public void setClientId(Long clientId) {
        this.clientId = clientId;
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
        return clientId;
    }
}
