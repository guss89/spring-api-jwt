package com.calisthenics.calisthenics.dto;

import com.calisthenics.calisthenics.model.catalogs.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Igual que UserRequest, pero la contraseña es opcional: si no viene, no se cambia. */
@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String lastName;

    @NotBlank
    @Size(min = 4, max = 50)
    private String username;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(min = 8, max = 72)
    @Pattern(regexp = "(?s).*\\S.*", message = "La contraseña no puede ser solo espacios")
    private String password;

    @NotNull
    private Role role;

    private Boolean active;
}
