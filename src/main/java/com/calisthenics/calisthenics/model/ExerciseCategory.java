package com.calisthenics.calisthenics.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "exercise_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(
            mappedBy = "exerciseCategory",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Exercise> exercises = new ArrayList<>();
}
