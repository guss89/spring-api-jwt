package com.calisthenics.calisthenics.dto;
import com.calisthenics.calisthenics.model.catalogs.Role;
import jakarta.validation.constraints.*;

public class RegisterRequest {

    @NotBlank
    private String name;

    @NotBlank
    private  String lastName;

    @NotBlank
    @Size(min = 4, max = 20)
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String phone;

    @NotBlank
    @Size(min = 8)
    private String password;

    private Role role;

    public String getName(){
        return name;
    }

    public String getLastName(){
        return lastName;
    }

    public String getUsername(){
        return username;
    }

    public String getEmail(){
        return email;
    }

    public String getPhone(){
        return phone;
    }

    public String getPassword(){
        return password;
    }

    public Role getRole(){
        return role;
    }
}
