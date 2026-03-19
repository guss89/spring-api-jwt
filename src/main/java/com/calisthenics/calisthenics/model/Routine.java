package com.calisthenics.calisthenics.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routine")
public class Routine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_routine_id", nullable = false)
    private TypeRoutine typeRoutine;

    @OneToMany(mappedBy = "routine")
    private List<ClientRoutine> clientRoutines = new ArrayList<>();


    /*Getters*/
    public Long getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public TypeRoutine getTypeRoutine(){
        return  typeRoutine;
    }

    public List<ClientRoutine> getClientRoutines() {
        return clientRoutines;
    }

    /*Setters*/
    public void setName(String name){
        this.name = name;
    }
    public void setTypeRoutine(TypeRoutine typeRoutine){ this.typeRoutine = typeRoutine; }

    public void addClientRoutine(ClientRoutine clientRoutine) {
        clientRoutines.add(clientRoutine);
        clientRoutine.setRoutine(this);
    }

    public void removeClientRoutine(ClientRoutine clientRoutine) {
        clientRoutines.remove(clientRoutine);
        clientRoutine.setRoutine(null);
    }
}
