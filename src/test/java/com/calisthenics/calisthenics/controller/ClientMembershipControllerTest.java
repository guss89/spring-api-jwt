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
import com.calisthenics.calisthenics.security.CurrentUser;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
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
                assertThat(result.getResponse().getStatus()).isEqualTo(401));
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(result ->
                assertThat(result.getResponse().getStatus()).isEqualTo(401));
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void getAll_sinFiltro_admin() throws Exception {
        when(service.getAll(isNull(), any())).thenReturn(List.of(response()));
        mockMvc.perform(get(URL)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        verify(service).getAll(isNull(), any());
    }

    // ---- Restricción por rol (el controlador pasa id y rol del llamador al servicio) ----

    private RequestPostProcessor auth(String id, String role) {
        return authentication(new UsernamePasswordAuthenticationToken(
                id, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    @Test
    void getAll_client_pasaSuIdYSinAdminAlServicio() throws Exception {
        when(service.getAll(isNull(), any())).thenReturn(List.of(response()));
        mockMvc.perform(get(URL).with(auth("1", "CLIENT")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        ArgumentCaptor<CurrentUser> captor = ArgumentCaptor.forClass(CurrentUser.class);
        verify(service).getAll(isNull(), captor.capture());
        assertThat(captor.getValue()).isEqualTo(new CurrentUser(1L, false, false, true));
    }

    @Test
    void getAll_admin_pasaAdminTrue() throws Exception {
        when(service.getAll(isNull(), any())).thenReturn(List.of(response()));
        mockMvc.perform(get(URL).with(auth("9", "ADMIN"))).andExpect(status().isOk());
        verify(service).getAll(isNull(), eq(new CurrentUser(9L, true)));
    }

    @Test
    void getAll_clientConOtroUserId_403() throws Exception {
        when(service.getAll(eq(7L), any())).thenThrow(new AccessDeniedException("Access denied"));
        mockMvc.perform(get(URL).param("userId", "7").with(auth("1", "CLIENT")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void getById_clientConIdAjeno_403() throws Exception {
        when(service.getById(eq(10L), any())).thenThrow(new AccessDeniedException("Access denied"));
        mockMvc.perform(get(URL + "/10").with(auth("2", "CLIENT"))).andExpect(status().isForbidden());
    }

    @Test
    void principalNoNumerico_403() throws Exception {
        mockMvc.perform(get(URL).with(auth("abc", "CLIENT"))).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void getAll_conUserId() throws Exception {
        when(service.getAll(eq(1L), any())).thenReturn(List.of(response()));
        mockMvc.perform(get(URL).param("userId", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].userId").value(1));
        verify(service).getAll(eq(1L), any());
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void getById_ok() throws Exception {
        when(service.getById(eq(10L), any())).thenReturn(response());
        mockMvc.perform(get(URL + "/10")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void getById_inexistente_404() throws Exception {
        when(service.getById(eq(99L), any())).thenThrow(new ResourceNotFoundException("Client membership not found"));
        mockMvc.perform(get(URL + "/99")).andExpect(status().isNotFound());
    }

    // ---- Escritura: solo ADMIN; CLIENT y COACH reciben 403 ----

    @Test
    void escritura_admin_funciona() throws Exception {
        when(service.save(any(), any())).thenReturn(response());
        when(service.update(eq(10L), any(), any())).thenReturn(response());

        mockMvc.perform(post(URL).with(auth("1", "ADMIN")).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk());
        mockMvc.perform(put(URL + "/10").with(auth("1", "ADMIN")).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk());
        mockMvc.perform(delete(URL + "/10").with(auth("1", "ADMIN"))).andExpect(status().isNoContent());
    }

    @Test
    void escritura_admin_conBodyVacio_400Json() throws Exception {
        mockMvc.perform(post(URL).with(auth("1", "ADMIN")).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.userId").exists());
        verifyNoInteractions(service);
    }

    @Test
    void escritura_client_403() throws Exception {
        escrituraProhibida("CLIENT");
    }

    @Test
    void escritura_coach_403() throws Exception {
        escrituraProhibida("COACH");
    }

    private void escrituraProhibida(String role) throws Exception {
        mockMvc.perform(post(URL).with(auth("1", role)).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(URL + "/10").with(auth("1", role)).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete(URL + "/10").with(auth("1", role))).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
}
