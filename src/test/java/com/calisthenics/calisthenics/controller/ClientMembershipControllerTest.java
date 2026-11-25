package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.config.SecurityConfig;
import com.calisthenics.calisthenics.dto.ClientMembershipRequest;
import com.calisthenics.calisthenics.dto.ClientMembershipResponse;
import com.calisthenics.calisthenics.exception.ConflictException;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.catalogs.MembershipStatus;
import com.calisthenics.calisthenics.repository.UserRepository;
import com.calisthenics.calisthenics.security.CustomUserDetailsService;
import com.calisthenics.calisthenics.service.ClientMembershipService;
import com.calisthenics.calisthenics.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientMembershipController.class)
@Import(SecurityConfig.class)
class ClientMembershipControllerTest {

    private static final String URL = "/api/v1/client-memberships";
    private static final String BODY = """
            {"userId":1,"membershipPlanId":2,"startDate":"2026-01-01"}
            """;

    @Autowired private MockMvc mockMvc;
    @MockBean private ClientMembershipService service;
    @MockBean private JwtService jwtService;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private UserRepository userRepository;

    private ClientMembershipResponse response() {
        return new ClientMembershipResponse(10L, 1L, 2L, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 30), MembershipStatus.ACTIVE, null);
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
        when(service.save(any(ClientMembershipRequest.class))).thenReturn(response());
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.endDate").value("2026-01-30"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
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
    void create_planInexistente_404() throws Exception {
        when(service.save(any())).thenThrow(new ResourceNotFoundException("Membership plan not found"));
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void create_reglaDeNegocio_400() throws Exception {
        when(service.save(any())).thenThrow(new IllegalArgumentException("User already has an active membership"));
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest());
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
        when(service.getById(99L)).thenThrow(new ResourceNotFoundException("Client membership not found"));
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
        when(service.update(eq(99L), any())).thenThrow(new ResourceNotFoundException("Client membership not found"));
        mockMvc.perform(put(URL + "/99").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void update_reglaDeNegocio_400() throws Exception {
        when(service.update(eq(10L), any())).thenThrow(new IllegalArgumentException("User must have role CLIENT"));
        mockMvc.perform(put(URL + "/10").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void update_reasignarConPagos_409() throws Exception {
        when(service.update(eq(10L), any()))
                .thenThrow(new ConflictException("Membership has associated payments and cannot be reassigned to another user"));
        mockMvc.perform(put(URL + "/10").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
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
        doThrow(new ResourceNotFoundException("Client membership not found")).when(service).delete(99L);
        mockMvc.perform(delete(URL + "/99")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void delete_conPagosAsociados_409() throws Exception {
        doThrow(new ConflictException("Membership has associated payments")).when(service).delete(10L);
        mockMvc.perform(delete(URL + "/10"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Membership has associated payments"));
    }

    // ---- Validación de entrada (400 Bad Request) ----

    private static final String SIN_USER_ID = """
            {"membershipPlanId":2,"startDate":"2026-01-01"}
            """;
    private static final String SIN_PLAN_ID = """
            {"userId":1,"startDate":"2026-01-01"}
            """;
    private static final String SIN_START_DATE = """
            {"userId":1,"membershipPlanId":2}
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

    @Test @WithMockUser void create_sinUserId_400() throws Exception { postEsperaBadRequest(SIN_USER_ID); }
    @Test @WithMockUser void create_sinMembershipPlanId_400() throws Exception { postEsperaBadRequest(SIN_PLAN_ID); }
    @Test @WithMockUser void create_sinStartDate_400() throws Exception { postEsperaBadRequest(SIN_START_DATE); }

    @Test @WithMockUser void update_sinUserId_400() throws Exception { putEsperaBadRequest(SIN_USER_ID); }
    @Test @WithMockUser void update_sinMembershipPlanId_400() throws Exception { putEsperaBadRequest(SIN_PLAN_ID); }
    @Test @WithMockUser void update_sinStartDate_400() throws Exception { putEsperaBadRequest(SIN_START_DATE); }
}
