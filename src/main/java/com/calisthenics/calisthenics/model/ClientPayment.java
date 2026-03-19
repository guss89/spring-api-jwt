package com.calisthenics.calisthenics.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.calisthenics.calisthenics.model.catalogs.Status;

@Entity
@Table(name = "client_payment")
public class ClientPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private LocalDate paymentDate;
    private BigDecimal amount;
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    /*Getters*/
    public Long getId(){ return id;}
    public LocalDate getPaymentDate(){ return paymentDate; }
    public BigDecimal getAmount(){ return amount; }
    public Status getStatus(){ return status; }
    public User getClient (){ return user; }
    public Long getClientId(){ return user != null ? user.getId() : null; }

    /*Setter*/
    public void setPaymentDate(LocalDate paymentDate){ this.paymentDate = paymentDate;}
    public void setAmount(BigDecimal amount){ this.amount = amount;}
    public void setStatus(Status status){ this.status = status; }
    public void setClient(User user) { this.user = user; }
}
