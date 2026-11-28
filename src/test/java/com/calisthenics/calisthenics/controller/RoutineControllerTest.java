package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.config.CorsConfig;
import com.calisthenics.calisthenics.config.SecurityConfig;
import com.calisthenics.calisthenics.dto.RoutineResponse;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.Routine;
import com.calisthenics.calisthenics.model.TypeRoutine;
import java.util.ArrayList;
import com.calisthenics.calisthenics.repository.UserRepository;
import com.calisthenics.calisthenics.security.CustomUserDetailsService;
import com.calisthenics.calisthenics.service.RoutineService;
import com.calisthenics.calisthenics.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoutineController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class})
@TestPropertySource(properties = "jwt.secret=test-secret-not-real-0123456789-abcdefghijkl")
class RoutineControllerTest {

    private static final String URL = "/api/v1/routines";
    private static final String BODY = """
            {"typeRoutineId":3,"name":"Rutina"}
            """;

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @MockBean private RoutineService service;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private UserRepository userRepository;

    private String auth() {
        return "Bearer " + jwtService.generateToken(1L, "ADMIN");
    }

    private RoutineResponse response() {
        TypeRoutine t = new TypeRoutine();
        t.setId(3L);
        t.setName("Pecho");
        Routine r = new Routine();
        r.setId(1L);
        r.setName("Rutina");
        r.setTypeRoutine(t);
        t.setRoutines(new ArrayList<>(List.of(r)));
        return RoutineResponse.from(r);
    }

    @Test
    void getAll_devuelve200SinRelacionesAnidadas() throws Exception {
        when(service.getAll()).thenReturn(List.of(response()));

        mockMvc.perform(get(URL).header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].typeRoutineId").value(3))
                .andExpect(jsonPath("$[0].typeRoutine").doesNotExist())
                .andExpect(jsonPath("$[0].clientRoutines").doesNotExist());
    }

    @Test
    void getAll_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
    }

    @Test
    void post_conRequestValido_devuelveResponse() throws Exception {
        when(service.save(any())).thenReturn(response());

        mockMvc.perform(post(URL).header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }


    @Test
    void put_inexistente_devuelve404() throws Exception {
        when(service.update(eq(99L), any())).thenThrow(new ResourceNotFoundException("no existe"));

        mockMvc.perform(put(URL + "/99").header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
    }
}
