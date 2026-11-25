package com.calisthenics.calisthenics.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exercise")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_weighted")
    private Boolean isWeighted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_category_id", nullable = false)
    private ExerciseCategory exerciseCategory;
}