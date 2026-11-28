package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.config.CorsConfig;
import com.calisthenics.calisthenics.config.SecurityConfig;
import com.calisthenics.calisthenics.dto.UserRequest;
import com.calisthenics.calisthenics.dto.UserResponse;
import com.calisthenics.calisthenics.dto.UserUpdateRequest;
import com.calisthenics.calisthenics.exception.ConflictException;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.catalogs.Role;
import org.springframework.http.MediaType;
import com.calisthenics.calisthenics.repository.UserRepository;
import com.calisthenics.calisthenics.security.CustomUserDetailsService;
import com.calisthenics.calisthenics.service.JwtService;
import com.calisthenics.calisthenics.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class})
@TestPropertySource(properties = "jwt.secret=test-secret-not-real-0123456789-abcdefghijkl")
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @MockBean private UserService userService;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private UserRepository userRepository;

    private static final String VALID_CREATE = """
            {"name":"Ana","lastName":"Lopez","username":"ana1","email":"a@b.com",
             "phone":"123","password":"password123","role":"COACH"}""";
    private static final String VALID_UPDATE = """
            {"name":"Ana","lastName":"Lopez","username":"ana1","email":"a@b.com",
             "phone":"123","role":"COACH"}""";

    private String bearer(Long id, String role) {
        return "Bearer " + jwtService.generateToken(id, role);
    }

    private UserResponse response() {
        return UserResponse.builder().id(3L).name("Ana").email("a@b.com").username("ana1")
                .role(Role.COACH).active(true).build();
    }

    @Test
    void getById_200() throws Exception {
        when(userService.getById(3L)).thenReturn(response());

        mockMvc.perform(get("/api/v1/users/3").header("Authorization", bearer(1L, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(content().string(not(containsString("passwordHash"))));
    }

    @Test
    void getById_404() throws Exception {
        when(userService.getById(99L)).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/99").header("Authorization", bearer(1L, "ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void post_201() throws Exception {
        when(userService.create(any(UserRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/v1/users").header("Authorization", bearer(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_CREATE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("COACH"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(content().string(not(containsString("password123"))));
    }

    @Test
    void post_bodyInvalido_400ConErrors() throws Exception {
        mockMvc.perform(post("/api/v1/users").header("Authorization", bearer(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.role").exists());
    }

    @Test
    void post_conflicto_409() throws Exception {
        when(userService.create(any(UserRequest.class))).thenThrow(new ConflictException("Email already registered"));

        mockMvc.perform(post("/api/v1/users").header("Authorization", bearer(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_CREATE))
                .andExpect(status().isConflict());
    }

    @Test
    void put_200() throws Exception {
        when(userService.update(eq(3L), any(UserUpdateRequest.class), eq(1L))).thenReturn(response());

        mockMvc.perform(put("/api/v1/users/3").header("Authorization", bearer(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void put_passwordSoloEspacios_400ConErrorsPassword() throws Exception {
        String body = """
                {"name":"Ana","lastName":"Lopez","username":"ana1","email":"a@b.com",
                 "phone":"123","password":"        ","role":"COACH"}""";

        mockMvc.perform(put("/api/v1/users/3").header("Authorization", bearer(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.password").exists());
        verifyNoInteractions(userService);
    }

    @Test
    void put_passwordConSaltoDeLinea_pasaValidacionDePatron() throws Exception {
        when(userService.update(eq(3L), any(UserUpdateRequest.class), eq(1L))).thenReturn(response());
        String body = """
                {"name":"Ana","lastName":"Lopez","username":"ana1","email":"a@b.com",
                 "phone":"123","password":"\\nabcdefgh","role":"COACH"}""";

        mockMvc.perform(put("/api/v1/users/3").header("Authorization", bearer(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }

    @Test
    void post_violacionDeIntegridad_409GenericoSinDetallesSql() throws Exception {
        when(userService.create(any(UserRequest.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException(
                        "Duplicate entry 'a@b.com' for key 'uk_users_email'"));

        mockMvc.perform(post("/api/v1/users").header("Authorization", bearer(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_CREATE))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Conflicto de integridad de datos: el registro está en uso o duplicado"))
                .andExpect(content().string(not(containsString("uk_users_email"))));
    }

    @Test
    void put_404() throws Exception {
        when(userService.update(eq(99L), any(UserUpdateRequest.class), eq(1L)))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(put("/api/v1/users/99").header("Authorization", bearer(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE))
                .andExpect(status().isNotFound());
    }

    @Test
    void put_409() throws Exception {
        when(userService.update(eq(3L), any(UserUpdateRequest.class), eq(1L)))
                .thenThrow(new ConflictException("Username already exists"));

        mockMvc.perform(put("/api/v1/users/3").header("Authorization", bearer(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE))
                .andExpect(status().isConflict());
    }

    @Test
    void put_autoProteccion_400() throws Exception {
        when(userService.update(eq(1L), any(UserUpdateRequest.class), eq(1L)))
                .thenThrow(new IllegalArgumentException("An admin cannot remove their own ADMIN role"));

        mockMvc.perform(put("/api/v1/users/1").header("Authorization", bearer(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_204() throws Exception {
        mockMvc.perform(delete("/api/v1/users/3").header("Authorization", bearer(1L, "ADMIN")))
                .andExpect(status().isNoContent());

        verify(userService).deactivate(3L, 1L);
    }

    @Test
    void delete_404() throws Exception {
        doThrow(new ResourceNotFoundException("User not found")).when(userService).deactivate(99L, 1L);

        mockMvc.perform(delete("/api/v1/users/99").header("Authorization", bearer(1L, "ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void clientYCoach_recibe403EnGetByIdYPost() throws Exception {
        for (String role : List.of("CLIENT", "COACH")) {
            mockMvc.perform(get("/api/v1/users/3").header("Authorization", bearer(2L, role)))
                    .andExpect(status().isForbidden());
            mockMvc.perform(post("/api/v1/users").header("Authorization", bearer(2L, role))
                            .contentType(MediaType.APPLICATION_JSON).content(VALID_CREATE))
                    .andExpect(status().isForbidden());
            mockMvc.perform(delete("/api/v1/users/3").header("Authorization", bearer(2L, role)))
                    .andExpect(status().isForbidden());
        }
        verifyNoInteractions(userService);
    }

    @Test
    void admin_recibe200SinPasswordHash() throws Exception {
        UserResponse user = UserResponse.builder().id(3L).name("Ana").email("a@b.com")
                .role(Role.CLIENT).active(true).build();
        when(userService.getAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + jwtService.generateToken(1L, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].email").value("a@b.com"))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist())
                .andExpect(content().string(not(containsString("secreto"))));
    }

    @Test
    void rolClient_recibe403() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + jwtService.generateToken(2L, "CLIENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void rolCoach_recibe403() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + jwtService.generateToken(2L, "COACH")))
                .andExpect(status().isForbidden());
    }

    @Test
    void tokenSinRol_recibe403EnUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + jwtService.generateToken(2L, null)))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinAutenticar_recibe401() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }
}
