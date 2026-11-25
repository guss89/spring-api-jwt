package com.calisthenics.calisthenics.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "type_routine")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeRoutine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(
            mappedBy = "typeRoutine",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Routine> routines = new ArrayList<>();

    // Helpers
    public void addRoutine(Routine routine) {
        routines.add(routine);
        routine.setTypeRoutine(this);
    }

    public void removeRoutine(Routine routine) {
        routines.remove(routine);
        routine.setTypeRoutine(null);
    }
}
