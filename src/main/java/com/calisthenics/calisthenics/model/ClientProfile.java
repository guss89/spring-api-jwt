package com.calisthenics.calisthenics.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="client")
public class ClientProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate birthdate;
    private double initialWeight;
    private double initialHeight;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /*Getters*/
    public  LocalDate getBirthdate(){
        return  birthdate;
    }
    public double getInitialWeight(){
        return  initialWeight;
    }
    public double getInitialHeight(){
        return  initialHeight;
    }
    public Long getId(){return  id;}
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public User getUser(){ return user; }

    /*Setters*/
    public void setBirthdate(LocalDate birthdate){ this.birthdate = birthdate; }
    public void setInitialWeight(double weight){
        initialWeight = weight;
    }
    public void setInitialHeight(double height){
        initialHeight = height;
    }
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt; }
    public void setUser(User user){ this.user = user; }
}
