package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.config.CorsConfig;
import com.calisthenics.calisthenics.config.SecurityConfig;
import com.calisthenics.calisthenics.dto.ClientRoutineResponse;
import com.calisthenics.calisthenics.model.*;
import java.time.LocalDate;
import com.calisthenics.calisthenics.repository.UserRepository;
import com.calisthenics.calisthenics.security.CurrentUser;
import com.calisthenics.calisthenics.security.CustomUserDetailsService;
import org.mockito.ArgumentCaptor;
import com.calisthenics.calisthenics.service.ClientRoutineService;
import com.calisthenics.calisthenics.service.JwtService;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientRoutineController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class})
@TestPropertySource(properties = "jwt.secret=test-secret-not-real-0123456789-abcdefghijkl")
class ClientRoutineControllerTest {

    private static final String URL = "/api/v1/client-routine";
    private static final String BODY = """
            {"userId":5,"routineId":3,"startDate":"2026-01-01","active":true}
            """;

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @MockBean private ClientRoutineService service;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private UserRepository userRepository;

    private String auth() {
        return "Bearer " + jwtService.generateToken(1L, "ADMIN");
    }

    private ClientRoutineResponse response() {
        User u = new User();
        u.setId(5L);
        Routine r = new Routine();
        r.setId(3L);
        ClientRoutine c = new ClientRoutine();
        c.setId(1L);
        c.setUser(u);
        c.setRoutine(r);
        c.setStartDate(LocalDate.of(2026, 1, 1));
        c.setActive(true);
        r.addClientRoutine(c);
        return ClientRoutineResponse.from(c);
    }

    @Test
    void getAll_devuelve200SinRelacionesAnidadas() throws Exception {
        when(service.getAll(any())).thenReturn(List.of(response()));

        mockMvc.perform(get(URL).header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userId").value(5))
                .andExpect(jsonPath("$[0].routineId").value(3))
                .andExpect(jsonPath("$[0].user").doesNotExist())
                .andExpect(jsonPath("$[0].routine").doesNotExist());
    }

    @Test
    void getAll_conClient_pasaSuIdAlServicio() throws Exception {
        when(service.getAll(any())).thenReturn(List.of(response()));

        mockMvc.perform(get(URL).header("Authorization", "Bearer " + jwtService.generateToken(5L, "CLIENT")))
                .andExpect(status().isOk());

        ArgumentCaptor<CurrentUser> captor = ArgumentCaptor.forClass(CurrentUser.class);
        verify(service).getAll(captor.capture());
        assertEquals(5L, captor.getValue().id());
        assertFalse(captor.getValue().admin());
    }

    @Test
    void getAll_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
    }

    private String coach() {
        return "Bearer " + jwtService.generateToken(8L, "COACH");
    }

    @Test
    void post_conCoach_devuelve200() throws Exception {
        when(service.save(any())).thenReturn(response());

        mockMvc.perform(post(URL).header("Authorization", coach())
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void post_conCoachBodyVacio_devuelve400Json() throws Exception {
        mockMvc.perform(post(URL).header("Authorization", coach())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.userId").exists())
                .andExpect(jsonPath("$.errors.routineId").exists())
                .andExpect(jsonPath("$.errors.startDate").exists());
        verifyNoInteractions(service);
    }

    @Test
    void put_conCoach_devuelve200() throws Exception {
        when(service.update(eq(1L), any())).thenReturn(response());

        mockMvc.perform(put(URL + "/1").header("Authorization", coach())
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routineId").value(3));
    }

    @Test
    void put_inexistente_devuelve404() throws Exception {
        when(service.update(eq(99L), any())).thenThrow(new ResourceNotFoundException("Client Routine not found"));

        mockMvc.perform(put(URL + "/99").header("Authorization", coach())
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_conCoach_devuelve204() throws Exception {
        mockMvc.perform(delete(URL + "/1").header("Authorization", coach()))
                .andExpect(status().isNoContent());
        verify(service).delete(1L);
    }

    @Test
    void delete_inexistente_devuelve404() throws Exception {
        doThrow(new ResourceNotFoundException("Client Routine not found")).when(service).delete(99L);

        mockMvc.perform(delete(URL + "/99").header("Authorization", coach()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_clientPropio_devuelve200YPasaElCaller() throws Exception {
        when(service.getById(eq(1L), any())).thenReturn(response());

        mockMvc.perform(get(URL + "/1").header("Authorization", "Bearer " + jwtService.generateToken(5L, "CLIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(service).getById(1L, new CurrentUser(5L, false, false, true));
    }

    @Test
    void getById_coach_pasaCoachTrue() throws Exception {
        when(service.getById(eq(1L), any())).thenReturn(response());

        mockMvc.perform(get(URL + "/1").header("Authorization", coach())).andExpect(status().isOk());

        verify(service).getById(1L, new CurrentUser(8L, false, true));
    }

    @Test
    void getById_ajeno_devuelve403() throws Exception {
        when(service.getById(eq(1L), any())).thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(get(URL + "/1").header("Authorization", "Bearer " + jwtService.generateToken(6L, "CLIENT")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void getById_inexistente_devuelve404() throws Exception {
        when(service.getById(eq(99L), any())).thenThrow(new ResourceNotFoundException("Client Routine not found"));

        mockMvc.perform(get(URL + "/99").header("Authorization", auth())).andExpect(status().isNotFound());
    }

    @Test
    void escrituras_conAdmin_devuelve403() throws Exception {
        mockMvc.perform(post(URL).header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(URL + "/1").header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete(URL + "/1").header("Authorization", auth()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void escrituras_conClient_devuelve403() throws Exception {
        String client = "Bearer " + jwtService.generateToken(2L, "CLIENT");
        mockMvc.perform(post(URL).header("Authorization", client)
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(URL + "/1").header("Authorization", client)
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete(URL + "/1").header("Authorization", client))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
}
