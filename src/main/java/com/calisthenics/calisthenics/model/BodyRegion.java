package com.calisthenics.calisthenics.model;

import jakarta.persistence.*;

@Entity
@Table(name = "body_region")
public class BodyRegion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    /*Getters*/
    public  Long getId(){
        return id;
    }
    public String getName(){
        return name;
    }

    /*Setters*/
    public void setName(String name){
        this.name = name;
    }

}
