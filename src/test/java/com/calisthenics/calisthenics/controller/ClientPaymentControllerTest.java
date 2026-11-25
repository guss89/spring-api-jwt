package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.ClientPaymentRequest;
import com.calisthenics.calisthenics.dto.ClientPaymentResponse;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.catalogs.PaymentStatus;
import com.calisthenics.calisthenics.repository.UserRepository;
import com.calisthenics.calisthenics.security.CustomUserDetailsService;
import com.calisthenics.calisthenics.service.ClientPaymentService;
import com.calisthenics.calisthenics.service.JwtService;
import com.calisthenics.calisthenics.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientPaymentController.class)
@Import(SecurityConfig.class)
class ClientPaymentControllerTest {

    private static final String URL = "/api/v1/client-payments";
    private static final String BODY = """
            {"amount":100.00,"paymentMethod":"CASH","paymentDate":"2026-01-10",
             "status":"PAID","userId":1,"clientMembershipId":5}
            """;

    @Autowired private MockMvc mockMvc;
    @MockBean private ClientPaymentService service;
    @MockBean private JwtService jwtService;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private UserRepository userRepository;

    private ClientPaymentResponse response() {
        return new ClientPaymentResponse(10L, new BigDecimal("100.00"), "CASH", LocalDate.of(2026, 1, 10),
                PaymentStatus.PAID, null, null, null, 1L, 5L);
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
        when(service.save(any(ClientPaymentRequest.class))).thenReturn(response());
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.clientMembershipId").value(5))
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    @WithMockUser
    void create_membresiaInexistente_404() throws Exception {
        when(service.save(any())).thenThrow(new ResourceNotFoundException("Membership not found"));
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void create_usuarioInexistente_404() throws Exception {
        when(service.save(any())).thenThrow(new ResourceNotFoundException("User not found"));
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getAll_sinFiltro() throws Exception {
        when(service.getAll(null)).thenReturn(List.of(response()));
        mockMvc.perform(get(URL)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        verify(service).getAll(null);
    }

    @Test
    @WithMockUser
    void getAll_conUserId() throws Exception {
        when(service.getAll(1L)).thenReturn(List.of(response()));
        mockMvc.perform(get(URL).param("userId", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].userId").value(1));
        verify(service).getAll(1L);
    }

    @Test
    @WithMockUser
    void getById_ok() throws Exception {
        when(service.getById(10L)).thenReturn(response());
        mockMvc.perform(get(URL + "/10")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @WithMockUser
    void getById_inexistente_404() throws Exception {
        when(service.getById(99L)).thenThrow(new ResourceNotFoundException("Client Payment not found"));
        mockMvc.perform(get(URL + "/99")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void update_ok() throws Exception {
        when(service.update(eq(10L), any())).thenReturn(response());
        mockMvc.perform(put(URL + "/10").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @WithMockUser
    void update_inexistente_404() throws Exception {
        when(service.update(eq(99L), any())).thenThrow(new ResourceNotFoundException("Client Payment not found"));
        mockMvc.perform(put(URL + "/99").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void delete_ok_204() throws Exception {
        mockMvc.perform(delete(URL + "/10")).andExpect(status().isNoContent());
        verify(service).delete(10L);
    }

    @Test
    @WithMockUser
    void delete_inexistente_404() throws Exception {
        doThrow(new ResourceNotFoundException("Client Payment not found")).when(service).delete(99L);
        mockMvc.perform(delete(URL + "/99")).andExpect(status().isNotFound());
    }

    // ---- Validación de entrada (400 Bad Request) ----

    private static final String SIN_AMOUNT = """
            {"paymentMethod":"CASH","paymentDate":"2026-01-10","status":"PAID","userId":1,"clientMembershipId":5}
            """;
    private static final String SIN_PAYMENT_DATE = """
            {"amount":100.00,"paymentMethod":"CASH","status":"PAID","userId":1,"clientMembershipId":5}
            """;
    private static final String SIN_USER_ID = """
            {"amount":100.00,"paymentMethod":"CASH","paymentDate":"2026-01-10","status":"PAID","clientMembershipId":5}
            """;
    private static final String SIN_MEMBERSHIP_ID = """
            {"amount":100.00,"paymentMethod":"CASH","paymentDate":"2026-01-10","status":"PAID","userId":1}
            """;
    private static final String AMOUNT_NEGATIVO = """
            {"amount":-10.00,"paymentMethod":"CASH","paymentDate":"2026-01-10","status":"PAID","userId":1,"clientMembershipId":5}
            """;
    private static final String AMOUNT_CERO = """
            {"amount":0,"paymentMethod":"CASH","paymentDate":"2026-01-10","status":"PAID","userId":1,"clientMembershipId":5}
            """;

    private void postEsperaBadRequest(String body) throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    private void putEsperaBadRequest(String body) throws Exception {
        mockMvc.perform(put(URL + "/10").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test @WithMockUser void create_sinAmount_400() throws Exception { postEsperaBadRequest(SIN_AMOUNT); }
    @Test @WithMockUser void create_sinPaymentDate_400() throws Exception { postEsperaBadRequest(SIN_PAYMENT_DATE); }
    @Test @WithMockUser void create_sinUserId_400() throws Exception { postEsperaBadRequest(SIN_USER_ID); }
    @Test @WithMockUser void create_sinClientMembershipId_400() throws Exception { postEsperaBadRequest(SIN_MEMBERSHIP_ID); }
    @Test @WithMockUser void create_amountNegativo_400() throws Exception { postEsperaBadRequest(AMOUNT_NEGATIVO); }
    @Test @WithMockUser void create_amountCero_400() throws Exception { postEsperaBadRequest(AMOUNT_CERO); }

    @Test @WithMockUser void update_sinAmount_400() throws Exception { putEsperaBadRequest(SIN_AMOUNT); }
    @Test @WithMockUser void update_sinPaymentDate_400() throws Exception { putEsperaBadRequest(SIN_PAYMENT_DATE); }
    @Test @WithMockUser void update_sinUserId_400() throws Exception { putEsperaBadRequest(SIN_USER_ID); }
    @Test @WithMockUser void update_sinClientMembershipId_400() throws Exception { putEsperaBadRequest(SIN_MEMBERSHIP_ID); }
    @Test @WithMockUser void update_amountNegativo_400() throws Exception { putEsperaBadRequest(AMOUNT_NEGATIVO); }
    @Test @WithMockUser void update_amountCero_400() throws Exception { putEsperaBadRequest(AMOUNT_CERO); }
}
