package com.calisthenics.calisthenics.model;

import com.calisthenics.calisthenics.model.catalogs.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(unique = true, length = 50)
    private String username;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.active == null) {
            this.active = true;
        }
    }
}
