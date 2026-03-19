package com.calisthenics.calisthenics.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "type_routine")
public class TypeRoutine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(
            mappedBy = "typeRoutine",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Routine> routines = new ArrayList<>();

    /* Getters */
    public Long getId() { return id; }
    public String getName() { return name; }
    public List<Routine> getRoutines() { return routines; }

    /* Setters */
    public void setName(String name) { this.name = name; }

    /* Helpers */
    public void addRoutine(Routine routine) {
        routines.add(routine);
        routine.setTypeRoutine(this);
    }

    public void removeRoutine(Routine routine) {
        routines.remove(routine);
        routine.setTypeRoutine(null);
    }

}
