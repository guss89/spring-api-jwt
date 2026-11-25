package com.calisthenics.calisthenics.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "body_region")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BodyRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
}