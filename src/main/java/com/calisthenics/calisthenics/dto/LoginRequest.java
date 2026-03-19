package com.calisthenics.calisthenics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {

    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    public String getEmail(){
        return email;
    }

    public String getPassword(){
        return password;
    }
}
