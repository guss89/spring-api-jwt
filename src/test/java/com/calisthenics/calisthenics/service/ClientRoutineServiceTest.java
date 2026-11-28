package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.*;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.*;
import com.calisthenics.calisthenics.model.catalogs.Role;
import com.calisthenics.calisthenics.repository.*;
import com.calisthenics.calisthenics.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientRoutineServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoutineRepository routineRepository;
    @Mock private ClientRoutineRepository clientRoutineRepository;
    @Mock private ClientRepository clientRepository;
    @InjectMocks private ClientRoutineService service;

    private User user() { return userWithRole(5L, Role.CLIENT); }
    private User userWithRole(Long id, Role role) {
        User u = new User();
        u.setId(id);
        u.setRole(role);
        return u;
    }
    private Routine routine() { Routine r = new Routine(); r.setId(3L); return r; }

    private ClientRoutineRequest request() {
        ClientRoutineRequest r = new ClientRoutineRequest();
        r.setUserId(5L);
        r.setRoutineId(3L);
        r.setStartDate(LocalDate.of(2026, 1, 1));
        r.setActive(true);
        return r;
    }

    private ClientRoutine existing() {
        ClientRoutine c = new ClientRoutine();
        c.setId(1L);
        c.setUser(user());
        c.setRoutine(routine());
        c.setStartDate(LocalDate.of(2025, 1, 1));
        c.setActive(false);
        return c;
    }

    @Test
    void getAll_devuelveDtos() {
        when(clientRoutineRepository.findAll()).thenReturn(List.of(existing()));

        List<ClientRoutineResponse> result = service.getAll(new CurrentUser(1L, true));

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getUserId());
        assertEquals(3L, result.get(0).getRoutineId());
    }

    @Test
    void getAll_noAdmin_soloConsultaLasSuyas() {
        when(clientRoutineRepository.findByUserId(5L)).thenReturn(List.of(existing()));

        List<ClientRoutineResponse> result = service.getAll(new CurrentUser(5L, false));

        assertEquals(1, result.size());
        verify(clientRoutineRepository, never()).findAll();
    }

    @Test
    void save_guardaYDevuelveDto() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user()));
        when(clientRepository.existsByUserId(5L)).thenReturn(true);
        when(routineRepository.findById(3L)).thenReturn(Optional.of(routine()));
        when(clientRoutineRepository.save(any(ClientRoutine.class))).thenAnswer(inv -> {
            ClientRoutine c = inv.getArgument(0);
            c.setId(7L);
            return c;
        });

        ClientRoutineResponse result = service.save(request());

        assertEquals(7L, result.getId());
        assertTrue(result.getActive());
    }

    @Test
    void save_usuarioAdminOCoach_lanzaIllegalArgument() {
        for (Role role : new Role[]{Role.ADMIN, Role.COACH}) {
            when(userRepository.findById(5L)).thenReturn(Optional.of(userWithRole(5L, role)));

            assertThrows(IllegalArgumentException.class, () -> service.save(request()));
        }
        verify(clientRoutineRepository, never()).save(any());
        verifyNoInteractions(routineRepository);
    }

    @Test
    void save_clienteSinPerfil_lanzaResourceNotFound() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user()));
        when(clientRepository.existsByUserId(5L)).thenReturn(false);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> service.save(request()));

        assertEquals("El cliente no tiene perfil registrado", ex.getMessage());
        verify(clientRoutineRepository, never()).save(any());
        verifyNoInteractions(routineRepository);
    }

    @Test
    void update_clienteSinPerfil_lanzaResourceNotFound() {
        when(clientRoutineRepository.findById(1L)).thenReturn(Optional.of(existing()));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user()));
        when(clientRepository.existsByUserId(5L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.update(1L, request()));
        verify(clientRoutineRepository, never()).save(any());
    }

    @Test
    void save_rutinaInexistente_lanzaResourceNotFound() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user()));
        when(clientRepository.existsByUserId(5L)).thenReturn(true);
        when(routineRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.save(request()));
        verify(clientRoutineRepository, never()).save(any());
    }

    @Test
    void update_usuarioInexistente_lanzaResourceNotFound() {
        when(clientRoutineRepository.findById(1L)).thenReturn(Optional.of(existing()));
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(1L, request()));
        verify(clientRoutineRepository, never()).save(any());
    }

    @Test
    void update_rutinaInexistente_lanzaResourceNotFound() {
        when(clientRoutineRepository.findById(1L)).thenReturn(Optional.of(existing()));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user()));
        when(clientRepository.existsByUserId(5L)).thenReturn(true);
        when(routineRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(1L, request()));
        verify(clientRoutineRepository, never()).save(any());
    }

    @Test
    void save_usuarioInexistente_lanzaResourceNotFound() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.save(request()));
        verify(clientRoutineRepository, never()).save(any());
    }

    @Test
    void update_cambiaElUserId_lanzaIllegalArgument() {
        when(clientRoutineRepository.findById(1L)).thenReturn(Optional.of(existing()));
        when(userRepository.findById(6L)).thenReturn(Optional.of(userWithRole(6L, Role.CLIENT)));
        when(clientRepository.existsByUserId(6L)).thenReturn(true);
        ClientRoutineRequest req = request();
        req.setUserId(6L);

        assertThrows(IllegalArgumentException.class, () -> service.update(1L, req));
        verify(clientRoutineRepository, never()).save(any());
    }

    @Test
    void update_usuarioNoClient_lanzaIllegalArgument() {
        when(clientRoutineRepository.findById(1L)).thenReturn(Optional.of(existing()));
        when(userRepository.findById(5L)).thenReturn(Optional.of(userWithRole(5L, Role.COACH)));

        assertThrows(IllegalArgumentException.class, () -> service.update(1L, request()));
        verify(clientRoutineRepository, never()).save(any());
    }

    @Test
    void update_devuelveDto() {
        ClientRoutine ex = existing();
        when(clientRoutineRepository.findById(1L)).thenReturn(Optional.of(ex));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user()));
        when(clientRepository.existsByUserId(5L)).thenReturn(true);
        when(routineRepository.findById(3L)).thenReturn(Optional.of(routine()));
        when(clientRoutineRepository.save(ex)).thenReturn(ex);

        ClientRoutineResponse result = service.update(1L, request());

        assertEquals(5L, result.getUserId());
        assertEquals(LocalDate.of(2026, 1, 1), result.getStartDate());
        assertTrue(result.getActive());
    }

    @Test
    void update_inexistente_lanzaResourceNotFound() {
        when(clientRoutineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(99L, request()));
        verify(clientRoutineRepository, never()).save(any());
    }

    @Test
    void getById_clientPropio_devuelveDto() {
        when(clientRoutineRepository.findById(1L)).thenReturn(Optional.of(existing()));

        assertEquals(1L, service.getById(1L, new CurrentUser(5L, false, false)).getId());
    }

    @Test
    void getById_clientAjeno_lanzaAccessDenied() {
        when(clientRoutineRepository.findById(1L)).thenReturn(Optional.of(existing()));

        assertThrows(AccessDeniedException.class,
                () -> service.getById(1L, new CurrentUser(6L, false, false)));
    }

    @Test
    void getById_coachYAdmin_veCualquiera() {
        when(clientRoutineRepository.findById(1L)).thenReturn(Optional.of(existing()));

        assertEquals(1L, service.getById(1L, new CurrentUser(8L, false, true)).getId());
        assertEquals(1L, service.getById(1L, new CurrentUser(1L, true, false)).getId());
    }

    @Test
    void getById_inexistente_lanzaResourceNotFound() {
        when(clientRoutineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getById(99L, new CurrentUser(5L, false, false)));
    }

    @Test
    void delete_eliminaElExistente() {
        ClientRoutine ex = existing();
        when(clientRoutineRepository.findById(1L)).thenReturn(Optional.of(ex));

        service.delete(1L);

        verify(clientRoutineRepository).delete(ex);
    }

    @Test
    void delete_inexistente_lanzaResourceNotFound() {
        when(clientRoutineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(99L));
        verify(clientRoutineRepository, never()).delete(any());
    }
}
