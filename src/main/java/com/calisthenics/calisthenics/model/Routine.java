package com.calisthenics.calisthenics.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "routine")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Routine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_routine_id", nullable = false)
    private TypeRoutine typeRoutine;

    @OneToMany(
            mappedBy = "routine",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<ClientRoutine> clientRoutines = new ArrayList<>();

    // Métodos helper para mantener consistencia bidireccional
    public void addClientRoutine(ClientRoutine clientRoutine) {
        clientRoutines.add(clientRoutine);
        clientRoutine.setRoutine(this);
    }

    public void removeClientRoutine(ClientRoutine clientRoutine) {
        clientRoutines.remove(clientRoutine);
        clientRoutine.setRoutine(null);
    }
}