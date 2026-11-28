package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.config.CorsConfig;
import com.calisthenics.calisthenics.config.SecurityConfig;
import com.calisthenics.calisthenics.dto.BodyRegionResponse;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.BodyRegion;
import com.calisthenics.calisthenics.repository.UserRepository;
import com.calisthenics.calisthenics.security.CustomUserDetailsService;
import com.calisthenics.calisthenics.service.BodyRegionService;
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

@WebMvcTest(BodyRegionController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class})
@TestPropertySource(properties = "jwt.secret=test-secret-not-real-0123456789-abcdefghijkl")
class BodyRegionControllerTest {

    private static final String URL = "/api/v1/body-regions";
    private static final String BODY = """
            {"name":"Pecho"}
            """;

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @MockBean private BodyRegionService service;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private UserRepository userRepository;

    private String auth() {
        return "Bearer " + jwtService.generateToken(1L, "ADMIN");
    }

    private BodyRegionResponse response() {
        BodyRegion e = new BodyRegion();
        e.setId(1L);
        e.setName("Pecho");
        return BodyRegionResponse.from(e);
    }

    @Test
    void getAll_devuelve200SinRelacionesAnidadas() throws Exception {
        when(service.getAll()).thenReturn(List.of(response()));

        mockMvc.perform(get(URL).header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Pecho"));
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
    void post_sinNombre_devuelve400() throws Exception {
        mockMvc.perform(post(URL).header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists());
    }


    @Test
    void put_inexistente_devuelve404() throws Exception {
        when(service.update(eq(99L), any())).thenThrow(new ResourceNotFoundException("no existe"));

        mockMvc.perform(put(URL + "/99").header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
    }
}
