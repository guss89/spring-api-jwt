package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.config.CorsConfig;
import com.calisthenics.calisthenics.config.SecurityConfig;
import com.calisthenics.calisthenics.dto.ClientProfileResponse;
import com.calisthenics.calisthenics.exception.ConflictException;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.ClientProfile;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.model.catalogs.Role;
import com.calisthenics.calisthenics.repository.UserRepository;
import com.calisthenics.calisthenics.security.CurrentUser;
import com.calisthenics.calisthenics.security.CustomUserDetailsService;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import com.calisthenics.calisthenics.service.ClientProfileService;
import com.calisthenics.calisthenics.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class})
@TestPropertySource(properties = "jwt.secret=test-secret-not-real-0123456789-abcdefghijkl")
class ClientControllerTest {

    private static final String BODY = """
            {"userId":5,"birthDate":"1990-05-10","initialWeight":70.5,"initialHeight":175.0}
            """;

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @MockBean private ClientProfileService service;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private UserRepository userRepository;

    private String auth() {
        return "Bearer " + jwtService.generateToken(1L, "ADMIN");
    }

    private ClientProfileResponse response() {
        User user = User.builder().id(5L).name("Ana").passwordHash("$2a$secreto").role(Role.CLIENT).build();
        ClientProfile profile = ClientProfile.builder().id(9L).user(user).birthDate(LocalDate.of(1990, 5, 10))
                .initialWeight(new BigDecimal("70.5")).initialHeight(new BigDecimal("175.0")).build();
        return ClientProfileResponse.from(profile);
    }

    @Test
    void getAll_noExponePasswordHash() throws Exception {
        when(service.getAll(any())).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/v1/clients").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9))
                .andExpect(jsonPath("$[0].userId").value(5))
                .andExpect(jsonPath("$[0].user").doesNotExist())
                .andExpect(content().string(not(containsString("passwordHash"))))
                .andExpect(content().string(not(containsString("secreto"))));
    }

    @Test
    void getAll_conClient_pasaSuIdAlServicio() throws Exception {
        when(service.getAll(any())).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/v1/clients")
                        .header("Authorization", "Bearer " + jwtService.generateToken(5L, "CLIENT")))
                .andExpect(status().isOk());

        ArgumentCaptor<CurrentUser> captor = ArgumentCaptor.forClass(CurrentUser.class);
        verify(service).getAll(captor.capture());
        assertEquals(5L, captor.getValue().id());
        assertFalse(captor.getValue().admin());
    }

    @Test
    void getById_ajeno_devuelve403() throws Exception {
        when(service.getById(eq(9L), any())).thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(get("/api/v1/clients/9")
                        .header("Authorization", "Bearer " + jwtService.generateToken(6L, "CLIENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_inexistente_devuelve404() throws Exception {
        when(service.getById(eq(99L), any())).thenThrow(new ResourceNotFoundException("Client profile not found"));

        mockMvc.perform(get("/api/v1/clients/99").header("Authorization", auth()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_propio_devuelve200() throws Exception {
        when(service.getById(eq(9L), any())).thenReturn(response());

        mockMvc.perform(get("/api/v1/clients/9")
                        .header("Authorization", "Bearer " + jwtService.generateToken(5L, "CLIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(5));
    }

    @Test
    void postYDelete_conClientOCoach_devuelve403() throws Exception {
        for (String role : new String[]{"CLIENT", "COACH"}) {
            String token = "Bearer " + jwtService.generateToken(5L, role);
            mockMvc.perform(post("/api/v1/clients").header("Authorization", token)
                            .contentType(MediaType.APPLICATION_JSON).content(BODY))
                    .andExpect(status().isForbidden());
            mockMvc.perform(delete("/api/v1/clients/9").header("Authorization", token))
                    .andExpect(status().isForbidden());
        }

        verifyNoInteractions(service);
    }

    @Test
    void put_conClientYCoach_pasaElCallerAlServicio() throws Exception {
        when(service.update(any(), any(), any())).thenReturn(response());

        for (String role : new String[]{"CLIENT", "COACH"}) {
            mockMvc.perform(put("/api/v1/clients/9")
                            .header("Authorization", "Bearer " + jwtService.generateToken(5L, role))
                            .contentType(MediaType.APPLICATION_JSON).content(BODY))
                    .andExpect(status().isOk());
        }
        verify(service).update(eq(9L), any(), eq(new CurrentUser(5L, false, false, true)));
        verify(service).update(eq(9L), any(), eq(new CurrentUser(5L, false, true)));
    }

    @Test
    void put_ajeno_devuelve403Json() throws Exception {
        when(service.update(any(), any(), any())).thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(put("/api/v1/clients/9")
                        .header("Authorization", "Bearer " + jwtService.generateToken(6L, "CLIENT"))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void post_bodyVacio_devuelve400ConErroresJson() throws Exception {
        mockMvc.perform(post("/api/v1/clients").header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.userId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void delete_conAdmin_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/v1/clients/9").header("Authorization", auth()))
                .andExpect(status().isNoContent());
    }

    @Test
    void post_conRequestValido_devuelveResponse() throws Exception {
        when(service.save(any())).thenReturn(response());

        mockMvc.perform(post("/api/v1/clients").header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.initialWeight").value(70.5));
    }

    @Test
    void post_usuarioNoExiste_devuelve404() throws Exception {
        when(service.save(any())).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(post("/api/v1/clients").header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void post_sinUserId_devuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/clients").header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"initialWeight\":70}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void put_devuelveResponse() throws Exception {
        when(service.update(any(), any(), any())).thenReturn(response());

        mockMvc.perform(put("/api/v1/clients/9").header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(5));
    }

    @Test
    void getAll_conTokenClientYCoach_devuelve200() throws Exception {
        when(service.getAll(any())).thenReturn(List.of());

        for (String role : new String[]{"CLIENT", "COACH"}) {
            mockMvc.perform(get("/api/v1/clients")
                            .header("Authorization", "Bearer " + jwtService.generateToken(2L, role)))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void getAll_conTokenSinRol_devuelve200() throws Exception {
        when(service.getAll(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/clients")
                        .header("Authorization", "Bearer " + jwtService.generateToken(2L, null)))
                .andExpect(status().isOk());
    }

    @Test
    void post_perfilDuplicado_devuelve409() throws Exception {
        when(service.save(any())).thenThrow(new ConflictException("Client profile already exists for this user"));

        mockMvc.perform(post("/api/v1/clients").header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void put_conOtroUserId_devuelve400() throws Exception {
        when(service.update(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Client profile cannot be reassigned to another user"));

        mockMvc.perform(put("/api/v1/clients/9").header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Client profile cannot be reassigned to another user"));
    }

    @Test
    void put_sinUserId_devuelve400() throws Exception {
        mockMvc.perform(put("/api/v1/clients/9").header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"initialWeight\":70}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void put_idInexistente_devuelve404() throws Exception {
        when(service.update(any(), any(), any())).thenThrow(new ResourceNotFoundException("Client profile not found"));

        mockMvc.perform(put("/api/v1/clients/99").header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_idInexistente_devuelve404() throws Exception {
        doThrow(new ResourceNotFoundException("Client profile not found")).when(service).delete(99L);

        mockMvc.perform(delete("/api/v1/clients/99").header("Authorization", auth()))
                .andExpect(status().isNotFound());
    }

    @Test
    void post_pesoNegativoOFechaFutura_devuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/clients").header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":5,\"initialWeight\":-1}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/clients").header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":5,\"birthDate\":\"2999-01-01\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void sinAutenticar_devuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isUnauthorized());
    }
}
