package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.config.SecurityConfig;
import com.calisthenics.calisthenics.dto.MembershipPlanRequest;
import com.calisthenics.calisthenics.dto.MembershipPlanResponse;
import com.calisthenics.calisthenics.exception.ConflictException;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.repository.UserRepository;
import com.calisthenics.calisthenics.security.CustomUserDetailsService;
import com.calisthenics.calisthenics.service.JwtService;
import com.calisthenics.calisthenics.service.MembershipPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MembershipPlanController.class)
@Import(SecurityConfig.class)
class MembershipPlanControllerTest {

    private static final String URL = "/api/v1/membership-plans";
    private static final String BODY = """
            {"name":"Mensual","price":30.00,"durationDays":30}
            """;

    @Autowired private MockMvc mockMvc;
    @MockBean private MembershipPlanService service;
    @MockBean private JwtService jwtService;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private UserRepository userRepository;

    private MembershipPlanResponse response() {
        return new MembershipPlanResponse(1L, "Mensual", new BigDecimal("30.00"), 30, null);
    }

    @Test
    void sinAutenticacion_rechaza() throws Exception {
        mockMvc.perform(get(URL)).andExpect(result ->
                assertThat(result.getResponse().getStatus()).isIn(401, 403));
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(result ->
                assertThat(result.getResponse().getStatus()).isIn(401, 403));
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser
    void create_ok() throws Exception {
        when(service.save(any(MembershipPlanRequest.class))).thenReturn(response());
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mensual"))
                .andExpect(jsonPath("$.durationDays").value(30));
    }

    @Test
    @WithMockUser
    void getAll_ok() throws Exception {
        when(service.getAll()).thenReturn(List.of(response()));
        mockMvc.perform(get(URL)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    void getById_ok() throws Exception {
        when(service.getById(1L)).thenReturn(response());
        mockMvc.perform(get(URL + "/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void getById_inexistente_404() throws Exception {
        when(service.getById(99L)).thenThrow(new ResourceNotFoundException("Membership plan not found"));
        mockMvc.perform(get(URL + "/99")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void update_ok() throws Exception {
        when(service.update(eq(1L), any())).thenReturn(response());
        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void update_inexistente_404() throws Exception {
        when(service.update(eq(99L), any())).thenThrow(new ResourceNotFoundException("Membership plan not found"));
        mockMvc.perform(put(URL + "/99").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void delete_ok_204() throws Exception {
        mockMvc.perform(delete(URL + "/1")).andExpect(status().isNoContent());
        verify(service).delete(1L);
    }

    @Test
    @WithMockUser
    void delete_inexistente_404() throws Exception {
        doThrow(new ResourceNotFoundException("Membership plan not found")).when(service).delete(99L);
        mockMvc.perform(delete(URL + "/99")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void delete_conMembresiasAsociadas_409() throws Exception {
        doThrow(new ConflictException("Membership plan has associated memberships")).when(service).delete(1L);
        mockMvc.perform(delete(URL + "/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Membership plan has associated memberships"));
    }

    // ---- Validación de entrada (400 Bad Request) ----

    private static final String SIN_NAME = """
            {"price":30.00,"durationDays":30}
            """;
    private static final String NAME_VACIO = """
            {"name":"  ","price":30.00,"durationDays":30}
            """;
    private static final String SIN_PRICE = """
            {"name":"Mensual","durationDays":30}
            """;
    private static final String PRICE_CERO = """
            {"name":"Mensual","price":0,"durationDays":30}
            """;
    private static final String PRICE_NEGATIVO = """
            {"name":"Mensual","price":-5.00,"durationDays":30}
            """;
    private static final String SIN_DURATION = """
            {"name":"Mensual","price":30.00}
            """;
    private static final String DURATION_CERO = """
            {"name":"Mensual","price":30.00,"durationDays":0}
            """;
    private static final String DURATION_NEGATIVA = """
            {"name":"Mensual","price":30.00,"durationDays":-3}
            """;

    private void postEsperaBadRequest(String body) throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    private void putEsperaBadRequest(String body) throws Exception {
        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    private static final String NAME_LARGO = "{\"name\":\"" + "a".repeat(101)
            + "\",\"price\":30.00,\"durationDays\":30}";
    private static final String PRICE_ENTEROS_EXCESO = """
            {"name":"Mensual","price":1000000,"durationDays":30}
            """;
    private static final String PRICE_TRES_DECIMALES = """
            {"name":"Mensual","price":10.123,"durationDays":30}
            """;

    @Test @WithMockUser void create_nameDe101Caracteres_400() throws Exception { postEsperaBadRequest(NAME_LARGO); }
    @Test @WithMockUser void create_priceUnMillon_400() throws Exception { postEsperaBadRequest(PRICE_ENTEROS_EXCESO); }
    @Test @WithMockUser void create_priceTresDecimales_400() throws Exception { postEsperaBadRequest(PRICE_TRES_DECIMALES); }
    @Test @WithMockUser void update_nameDe101Caracteres_400() throws Exception { putEsperaBadRequest(NAME_LARGO); }
    @Test @WithMockUser void update_priceUnMillon_400() throws Exception { putEsperaBadRequest(PRICE_ENTEROS_EXCESO); }
    @Test @WithMockUser void update_priceTresDecimales_400() throws Exception { putEsperaBadRequest(PRICE_TRES_DECIMALES); }

    @Test @WithMockUser void create_sinName_400() throws Exception { postEsperaBadRequest(SIN_NAME); }
    @Test @WithMockUser void create_nameVacio_400() throws Exception { postEsperaBadRequest(NAME_VACIO); }
    @Test @WithMockUser void create_sinPrice_400() throws Exception { postEsperaBadRequest(SIN_PRICE); }
    @Test @WithMockUser void create_priceCero_400() throws Exception { postEsperaBadRequest(PRICE_CERO); }
    @Test @WithMockUser void create_priceNegativo_400() throws Exception { postEsperaBadRequest(PRICE_NEGATIVO); }
    @Test @WithMockUser void create_sinDurationDays_400() throws Exception { postEsperaBadRequest(SIN_DURATION); }
    @Test @WithMockUser void create_durationDaysCero_400() throws Exception { postEsperaBadRequest(DURATION_CERO); }
    @Test @WithMockUser void create_durationDaysNegativa_400() throws Exception { postEsperaBadRequest(DURATION_NEGATIVA); }

    @Test @WithMockUser void update_sinName_400() throws Exception { putEsperaBadRequest(SIN_NAME); }
    @Test @WithMockUser void update_sinPrice_400() throws Exception { putEsperaBadRequest(SIN_PRICE); }
    @Test @WithMockUser void update_priceNegativo_400() throws Exception { putEsperaBadRequest(PRICE_NEGATIVO); }
    @Test @WithMockUser void update_sinDurationDays_400() throws Exception { putEsperaBadRequest(SIN_DURATION); }
    @Test @WithMockUser void update_durationDaysCero_400() throws Exception { putEsperaBadRequest(DURATION_CERO); }
}
