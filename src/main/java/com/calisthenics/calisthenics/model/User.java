package com.calisthenics.calisthenics.model;

import com.calisthenics.calisthenics.model.catalogs.Role;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String lastname;
    private String username;
    private  String phone;
    private  String email;
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    private Role role;
    private Boolean active;
    private LocalDateTime createdAt;


    /*Getters*/
    public Long getId(){ return id;}
    public String getName() {return name;}
    public String getLastName() {return lastname;}
    public String getUsername() {return username;}
    public String getPhone() {return phone;}
    public String getEmail() {return email;}
    public String getPasswordHash() {return passwordHash;}
    public Role getRole() {return role;}
    public Boolean getActive() {return active;}
    public LocalDateTime getCreateAt() {return createdAt;}

    /*Setters*/
    public void setName(String name){this.name = name;}
    public void setLastName(String lastname){this.lastname = lastname;}
    public void setUsername(String username){this.username = username;}
    public void setPhone(String phone){ this.phone = phone; }
    public void setEmail(String email){ this.email = email; }
    public void setPasswordHash(String passwordHash){ this.passwordHash = passwordHash; }
    public void setRole(Role role){ this.role = role; }
    public void setActive(Boolean active) {this.active = active; }
}
