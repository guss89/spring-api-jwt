package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.CoachClientRequest;
import com.calisthenics.calisthenics.dto.CoachClientResponse;
import com.calisthenics.calisthenics.exception.ConflictException;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.CoachClient;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.model.catalogs.Role;
import com.calisthenics.calisthenics.repository.ClientRepository;
import com.calisthenics.calisthenics.repository.CoachClientRepository;
import com.calisthenics.calisthenics.repository.UserRepository;
import com.calisthenics.calisthenics.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoachClientServiceTest {

    private static final CurrentUser ADMIN = new CurrentUser(1L, true, false);

    @Mock private CoachClientRepository coachClientRepository;
    @Mock private UserRepository userRepository;
    @Mock private ClientRepository clientRepository;
    @InjectMocks private CoachClientService service;

    private User user(Long id, Role role) {
        User u = new User();
        u.setId(id);
        u.setRole(role);
        return u;
    }

    private CoachClientRequest request() {
        return new CoachClientRequest(2L, 3L);
    }

    private CoachClient relation() {
        return CoachClient.builder().id(10L).coach(user(2L, Role.COACH)).client(user(3L, Role.CLIENT)).build();
    }

    @Test
    void assign_admin_creaLaRelacion() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, Role.COACH)));
        when(userRepository.findById(3L)).thenReturn(Optional.of(user(3L, Role.CLIENT)));
        when(clientRepository.existsByUserId(3L)).thenReturn(true);
        when(coachClientRepository.existsByCoachIdAndClientId(2L, 3L)).thenReturn(false);
        when(coachClientRepository.save(any(CoachClient.class))).thenAnswer(inv -> {
            CoachClient c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        CoachClientResponse result = service.assignCoachToClient(request(), ADMIN);

        assertEquals(10L, result.getId());
        assertEquals(2L, result.getCoachId());
        assertEquals(3L, result.getClientId());
    }

    @Test
    void assign_noAdmin_lanzaAccessDenied() {
        assertThrows(AccessDeniedException.class,
                () -> service.assignCoachToClient(request(), new CurrentUser(2L, false, true)));
        verifyNoInteractions(userRepository, coachClientRepository);
    }

    @Test
    void assign_coachInexistente_lanzaNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.assignCoachToClient(request(), ADMIN));
        verify(coachClientRepository, never()).save(any());
    }

    @Test
    void assign_clienteInexistente_lanzaNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, Role.COACH)));
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.assignCoachToClient(request(), ADMIN));
        verify(coachClientRepository, never()).save(any());
    }

    @Test
    void assign_coachSinRolCoach_lanzaIllegalArgument() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, Role.CLIENT)));
        when(userRepository.findById(3L)).thenReturn(Optional.of(user(3L, Role.CLIENT)));

        assertThrows(IllegalArgumentException.class, () -> service.assignCoachToClient(request(), ADMIN));
        verify(coachClientRepository, never()).save(any());
    }

    @Test
    void assign_clienteSinRolClient_lanzaIllegalArgument() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, Role.COACH)));
        when(userRepository.findById(3L)).thenReturn(Optional.of(user(3L, Role.COACH)));

        assertThrows(IllegalArgumentException.class, () -> service.assignCoachToClient(request(), ADMIN));
        verify(coachClientRepository, never()).save(any());
    }

    @Test
    void assign_clienteSinPerfil_lanzaNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, Role.COACH)));
        when(userRepository.findById(3L)).thenReturn(Optional.of(user(3L, Role.CLIENT)));
        when(clientRepository.existsByUserId(3L)).thenReturn(false);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.assignCoachToClient(request(), ADMIN));

        assertEquals("El cliente no tiene perfil registrado", ex.getMessage());
        verify(coachClientRepository, never()).save(any());
    }

    @Test
    void assign_duplicada_lanzaConflict() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, Role.COACH)));
        when(userRepository.findById(3L)).thenReturn(Optional.of(user(3L, Role.CLIENT)));
        when(clientRepository.existsByUserId(3L)).thenReturn(true);
        when(coachClientRepository.existsByCoachIdAndClientId(2L, 3L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.assignCoachToClient(request(), ADMIN));
        verify(coachClientRepository, never()).save(any());
    }

    @Test
    void getClientsByCoach_coachPropio_devuelveLista() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, Role.COACH)));
        when(coachClientRepository.findByCoachId(2L)).thenReturn(List.of(relation()));

        List<CoachClientResponse> result = service.getClientsByCoach(2L, new CurrentUser(2L, false, true));

        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getClientId());
    }

    @Test
    void getClientsByCoach_coachAjeno_lanzaAccessDenied() {
        assertThrows(AccessDeniedException.class,
                () -> service.getClientsByCoach(2L, new CurrentUser(9L, false, true)));
        verifyNoInteractions(coachClientRepository);
    }

    @Test
    void getClientsByCoach_clientConMismoId_lanzaAccessDenied() {
        assertThrows(AccessDeniedException.class,
                () -> service.getClientsByCoach(2L, new CurrentUser(2L, false, false)));
    }

    @Test
    void getClientsByCoach_admin_veCualquiera() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, Role.COACH)));
        when(coachClientRepository.findByCoachId(2L)).thenReturn(List.of(relation()));

        assertEquals(1, service.getClientsByCoach(2L, ADMIN).size());
    }

    @Test
    void getClientsByCoach_inexistente_lanzaNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getClientsByCoach(99L, ADMIN));
    }

    @Test
    void getCoachesByClient_clientPropio_devuelveLista() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(user(3L, Role.CLIENT)));
        when(coachClientRepository.findByClientId(3L)).thenReturn(List.of(relation()));

        List<CoachClientResponse> result = service.getCoachesByClient(3L, new CurrentUser(3L, false, false, true));

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getCoachId());
    }

    @Test
    void getCoachesByClient_clientAjeno_lanzaAccessDenied() {
        assertThrows(AccessDeniedException.class,
                () -> service.getCoachesByClient(3L, new CurrentUser(9L, false, false, true)));
        verifyNoInteractions(coachClientRepository);
    }

    @Test
    void getCoachesByClient_coachConMismoId_lanzaAccessDenied() {
        assertThrows(AccessDeniedException.class,
                () -> service.getCoachesByClient(2L, new CurrentUser(2L, false, true, false)));
        verifyNoInteractions(coachClientRepository);
    }

    @Test
    void getCoachesByClient_admin_veCualquiera() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(user(3L, Role.CLIENT)));
        when(coachClientRepository.findByClientId(3L)).thenReturn(List.of(relation()));

        assertEquals(1, service.getCoachesByClient(3L, ADMIN).size());
    }

    @Test
    void getCoachesByClient_inexistente_lanzaNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getCoachesByClient(99L, ADMIN));
    }

    @Test
    void delete_admin_eliminaLaRelacion() {
        CoachClient rel = relation();
        when(coachClientRepository.findById(10L)).thenReturn(Optional.of(rel));

        service.delete(10L, ADMIN);

        verify(coachClientRepository).delete(rel);
    }

    @Test
    void delete_noAdmin_lanzaAccessDenied() {
        assertThrows(AccessDeniedException.class, () -> service.delete(10L, new CurrentUser(2L, false, true)));
        verifyNoInteractions(coachClientRepository);
    }

    @Test
    void delete_inexistente_lanzaNotFound() {
        when(coachClientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(99L, ADMIN));
        verify(coachClientRepository, never()).delete(any());
    }
}
