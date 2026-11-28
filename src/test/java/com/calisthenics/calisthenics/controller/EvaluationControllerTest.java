package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.config.CorsConfig;
import com.calisthenics.calisthenics.config.SecurityConfig;
import com.calisthenics.calisthenics.dto.EvaluationResponse;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.repository.UserRepository;
import com.calisthenics.calisthenics.security.CurrentUser;
import com.calisthenics.calisthenics.security.CustomUserDetailsService;
import com.calisthenics.calisthenics.service.EvaluationService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EvaluationController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class})
@TestPropertySource(properties = "jwt.secret=test-secret-not-real-0123456789-abcdefghijkl")
class EvaluationControllerTest {

    private static final String URL = "/api/v1/evaluations";
    private static final String BODY = """
            {"userId":5,"exerciseId":3,"maxReps":10,"maxWeight":20.5,"registerDate":"2026-01-01"}
            """;

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @MockBean private EvaluationService service;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private UserRepository userRepository;

    private String token(Long id, String role) {
        return "Bearer " + jwtService.generateToken(id, role);
    }

    private EvaluationResponse response() {
        return new EvaluationResponse(1L, 10L, new BigDecimal("20.5"), "n", LocalDate.of(2026, 1, 1), 3L, 5L);
    }

    @Test
    void getAll_conClient_pasaSuIdAlServicio() throws Exception {
        when(service.getAll(any(), any())).thenReturn(List.of(response()));

        mockMvc.perform(get(URL).header("Authorization", token(5L, "CLIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(5))
                .andExpect(jsonPath("$[0].exerciseId").value(3))
                .andExpect(jsonPath("$[0].user").doesNotExist());

        ArgumentCaptor<CurrentUser> captor = ArgumentCaptor.forClass(CurrentUser.class);
        verify(service).getAll(isNull(), captor.capture());
        assertEquals(5L, captor.getValue().id());
        assertFalse(captor.getValue().admin());
    }

    @Test
    void getAll_coachAsignadoConUserId_devuelve200() throws Exception {
        when(service.getAll(eq(5L), any())).thenReturn(List.of(response()));

        mockMvc.perform(get(URL).param("userId", "5").header("Authorization", token(10L, "COACH")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(5));

        ArgumentCaptor<CurrentUser> captor = ArgumentCaptor.forClass(CurrentUser.class);
        verify(service).getAll(eq(5L), captor.capture());
        assertEquals(10L, captor.getValue().id());
        assertTrue(captor.getValue().coach());
    }

    @Test
    void getAll_coachNoAsignadoConUserId_devuelve403Json() throws Exception {
        when(service.getAll(eq(7L), any())).thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(get(URL).param("userId", "7").header("Authorization", token(10L, "COACH")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void getAll_adminConUserId_devuelve200() throws Exception {
        when(service.getAll(eq(5L), any())).thenReturn(List.of(response()));

        mockMvc.perform(get(URL).param("userId", "5").header("Authorization", token(1L, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(5));
    }

    @Test
    void getById_ajeno_devuelve403() throws Exception {
        when(service.getById(eq(1L), any())).thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(get(URL + "/1").header("Authorization", token(6L, "CLIENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_inexistente_devuelve404() throws Exception {
        when(service.getById(eq(99L), any())).thenThrow(new ResourceNotFoundException("Evaluation not found"));

        mockMvc.perform(get(URL + "/99").header("Authorization", token(1L, "ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
    }

    @Test
    void escrituras_conClientOCoach_devuelve403() throws Exception {
        for (String role : new String[]{"CLIENT", "COACH"}) {
            String token = token(5L, role);
            mockMvc.perform(post(URL).header("Authorization", token)
                            .contentType(MediaType.APPLICATION_JSON).content(BODY))
                    .andExpect(status().isForbidden());
            mockMvc.perform(put(URL + "/1").header("Authorization", token)
                            .contentType(MediaType.APPLICATION_JSON).content(BODY))
                    .andExpect(status().isForbidden());
            mockMvc.perform(delete(URL + "/1").header("Authorization", token))
                    .andExpect(status().isForbidden());
        }

        verifyNoInteractions(service);
    }

    @Test
    void escrituras_conAdmin_funcionan() throws Exception {
        when(service.save(any())).thenReturn(response());
        when(service.update(eq(1L), any())).thenReturn(response());

        mockMvc.perform(post(URL).header("Authorization", token(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        mockMvc.perform(put(URL + "/1").header("Authorization", token(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk());
        mockMvc.perform(delete(URL + "/1").header("Authorization", token(1L, "ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void escrituras_bodyVacio_devuelve400ConErroresJson() throws Exception {
        mockMvc.perform(post(URL).header("Authorization", token(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.userId").exists())
                .andExpect(jsonPath("$.errors.exerciseId").exists())
                .andExpect(jsonPath("$.errors.registerDate").exists());
        mockMvc.perform(put(URL + "/1").header("Authorization", token(1L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.userId").exists());

        verifyNoInteractions(service);
    }
}
