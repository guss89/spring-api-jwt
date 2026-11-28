package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.config.CorsConfig;
import com.calisthenics.calisthenics.config.SecurityConfig;
import com.calisthenics.calisthenics.dto.CoachClientResponse;
import com.calisthenics.calisthenics.exception.ConflictException;
import com.calisthenics.calisthenics.exception.ForbiddenOperationException;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.repository.UserRepository;
import com.calisthenics.calisthenics.security.CurrentUser;
import com.calisthenics.calisthenics.security.CustomUserDetailsService;
import com.calisthenics.calisthenics.service.CoachClientService;
import com.calisthenics.calisthenics.service.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CoachClientController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class})
@TestPropertySource(properties = "jwt.secret=test-secret-not-real-0123456789-abcdefghijkl")
class CoachClientControllerTest {

    private static final String BODY = "{\"coachId\":2,\"clientId\":3}";

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @MockBean private CoachClientService service;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private UserRepository userRepository;

    private String token(Long id, String role) {
        return "Bearer " + jwtService.generateToken(id, role);
    }

    private CoachClientResponse response() {
        return new CoachClientResponse(10L, 2L, 3L);
    }

    @Test
    void post_admin_devuelve201ConDto() throws Exception {
        when(service.assignCoachToClient(any(), any())).thenReturn(response());

        mockMvc.perform(post("/api/v1/coach-clients").header("Authorization", token(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.coachId").value(2))
                .andExpect(jsonPath("$.clientId").value(3))
                .andExpect(content().string(not(containsString("passwordHash"))));
    }

    @Test
    void post_conClientOCoach_devuelve403() throws Exception {
        for (String role : new String[]{"CLIENT", "COACH"}) {
            mockMvc.perform(post("/api/v1/coach-clients").header("Authorization", token(5L, role))
                            .contentType(MediaType.APPLICATION_JSON).content(BODY))
                    .andExpect(status().isForbidden());
        }
        verifyNoInteractions(service);
    }

    @Test
    void post_sinToken_devuelve401() throws Exception {
        mockMvc.perform(post("/api/v1/coach-clients")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void post_sinCampos_devuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/coach-clients").header("Authorization", token(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.coachId").exists())
                .andExpect(jsonPath("$.errors.clientId").exists());
    }

    @Test
    void post_duplicada_devuelve409() throws Exception {
        when(service.assignCoachToClient(any(), any())).thenThrow(new ConflictException("Esta relación ya existe"));

        mockMvc.perform(post("/api/v1/coach-clients").header("Authorization", token(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void post_clienteSinPerfil_devuelve404() throws Exception {
        when(service.assignCoachToClient(any(), any()))
                .thenThrow(new ResourceNotFoundException("El cliente no tiene perfil registrado"));

        mockMvc.perform(post("/api/v1/coach-clients").header("Authorization", token(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("El cliente no tiene perfil registrado"));
    }

    @Test
    void post_rolIncorrecto_devuelve400() throws Exception {
        when(service.assignCoachToClient(any(), any()))
                .thenThrow(new IllegalArgumentException("El usuario no tiene rol COACH"));

        mockMvc.perform(post("/api/v1/coach-clients").header("Authorization", token(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_admin_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/v1/coach-clients/10").header("Authorization", token(1L, "ADMIN")))
                .andExpect(status().isNoContent());

        verify(service).delete(eq(10L), any());
    }

    @Test
    void delete_conCoach_devuelve403() throws Exception {
        mockMvc.perform(delete("/api/v1/coach-clients/10").header("Authorization", token(2L, "COACH")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void delete_inexistente_devuelve404() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Coach-client relation not found"))
                .when(service).delete(eq(99L), any());

        mockMvc.perform(delete("/api/v1/coach-clients/99").header("Authorization", token(1L, "ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void get_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/coach-clients").param("coachId", "2"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void get_porCoach_pasaElCallerAlServicio() throws Exception {
        when(service.getClientsByCoach(eq(2L), any())).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/v1/coach-clients").param("coachId", "2")
                        .header("Authorization", token(2L, "COACH")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientId").value(3));

        ArgumentCaptor<CurrentUser> captor = ArgumentCaptor.forClass(CurrentUser.class);
        verify(service).getClientsByCoach(eq(2L), captor.capture());
        assertEquals(2L, captor.getValue().id());
        assertTrue(captor.getValue().coach());
    }

    @Test
    void get_porClient_devuelveCoaches() throws Exception {
        when(service.getCoachesByClient(eq(3L), any())).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/v1/coach-clients").param("clientId", "3")
                        .header("Authorization", token(3L, "CLIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].coachId").value(2));
    }

    @Test
    void get_ajeno_devuelve403() throws Exception {
        when(service.getClientsByCoach(eq(2L), any())).thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(get("/api/v1/coach-clients").param("coachId", "2")
                        .header("Authorization", token(9L, "COACH")))
                .andExpect(status().isForbidden());
    }

    @Test
    void get_sinParametros_devuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/coach-clients").header("Authorization", token(1L, "ADMIN")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_conAmbosParametros_devuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/coach-clients").param("coachId", "2").param("clientId", "3")
                        .header("Authorization", token(1L, "ADMIN")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_inexistente_devuelve404() throws Exception {
        when(service.getClientsByCoach(eq(99L), any())).thenThrow(new ResourceNotFoundException("Coach not found"));

        mockMvc.perform(get("/api/v1/coach-clients").param("coachId", "99")
                        .header("Authorization", token(1L, "ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void forbiddenOperationException_devuelve403Json() throws Exception {
        when(service.getClientsByCoach(eq(2L), any())).thenThrow(new ForbiddenOperationException("No permitido"));

        mockMvc.perform(get("/api/v1/coach-clients").param("coachId", "2")
                        .header("Authorization", token(1L, "ADMIN")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("No permitido"));
    }

    @Test
    void illegalStateException_noSeConvierteEn403() throws Exception {
        when(service.getClientsByCoach(eq(2L), any())).thenThrow(new IllegalStateException("bug interno"));

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class,
                () -> mockMvc.perform(get("/api/v1/coach-clients").param("coachId", "2")
                        .header("Authorization", token(1L, "ADMIN"))));
    }
}
