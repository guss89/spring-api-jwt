package com.calisthenics.calisthenics.dto;
import com.calisthenics.calisthenics.model.catalogs.Role;
import jakarta.validation.constraints.*;

public class RegisterRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 100)
    private  String lastName;

    @NotBlank
    @Size(min = 4, max = 50)
    private String username;

    @Email
    @NotBlank
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(max = 20)
    private String phone;

    @NotBlank
    @Size(min = 8, max = 72)
    @Pattern(regexp = "(?s).*\\S.*", message = "La contraseña no puede ser solo espacios")
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
