package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.ClientProfileRequest;
import com.calisthenics.calisthenics.dto.ClientProfileResponse;
import com.calisthenics.calisthenics.exception.ConflictException;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.ClientProfile;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.model.catalogs.Role;
import com.calisthenics.calisthenics.repository.ClientRepository;
import com.calisthenics.calisthenics.repository.UserRepository;
import com.calisthenics.calisthenics.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientProfileServiceTest {

    @Mock private ClientRepository repository;
    @Mock private UserRepository userRepository;
    @InjectMocks private ClientProfileService service;

    private ClientProfileRequest request(Long userId) {
        return new ClientProfileRequest(userId, LocalDate.of(1990, 5, 10),
                new BigDecimal("70.5"), new BigDecimal("175.0"));
    }

    private User user(Long id, Role role) {
        return User.builder().id(id).name("Ana").role(role).build();
    }

    @Test
    void save_creaPerfilParaClient() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user(5L, Role.CLIENT)));
        when(repository.existsByUserId(5L)).thenReturn(false);
        when(repository.save(any(ClientProfile.class))).thenAnswer(inv -> {
            ClientProfile p = inv.getArgument(0);
            p.setId(9L);
            return p;
        });

        ClientProfileResponse response = service.save(request(5L));

        assertEquals(9L, response.getId());
        assertEquals(5L, response.getUserId());
        assertEquals(new BigDecimal("70.5"), response.getInitialWeight());
    }

    @Test
    void save_usuarioNoExiste_lanzaResourceNotFound() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.save(request(5L)));
        verify(repository, never()).save(any());
    }

    @Test
    void save_usuarioSinRolClient_lanzaIllegalArgument() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user(5L, Role.COACH)));

        assertThrows(IllegalArgumentException.class, () -> service.save(request(5L)));
        verify(repository, never()).save(any());
    }

    @Test
    void save_perfilDuplicado_lanzaConflict() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user(5L, Role.CLIENT)));
        when(repository.existsByUserId(5L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.save(request(5L)));
        verify(repository, never()).save(any());
    }

    @Test
    void getAll_admin_devuelveTodos() {
        ClientProfile p = ClientProfile.builder().id(9L).user(user(5L, Role.CLIENT)).build();
        when(repository.findAll()).thenReturn(java.util.List.of(p));

        assertEquals(1, service.getAll(new CurrentUser(1L, true)).size());
    }

    @Test
    void getAll_noAdmin_soloDevuelveElSuyo() {
        ClientProfile p = ClientProfile.builder().id(9L).user(user(5L, Role.CLIENT)).build();
        when(repository.findByUserId(5L)).thenReturn(Optional.of(p));

        var result = service.getAll(new CurrentUser(5L, false));

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getUserId());
        verify(repository, never()).findAll();
    }

    @Test
    void getAll_noAdminSinPerfil_devuelveVacio() {
        when(repository.findByUserId(5L)).thenReturn(Optional.empty());

        assertTrue(service.getAll(new CurrentUser(5L, false)).isEmpty());
    }

    @Test
    void getById_propio_devuelveResponse() {
        ClientProfile p = ClientProfile.builder().id(9L).user(user(5L, Role.CLIENT)).build();
        when(repository.findById(9L)).thenReturn(Optional.of(p));

        assertEquals(9L, service.getById(9L, new CurrentUser(5L, false)).getId());
    }

    @Test
    void getById_ajeno_lanzaAccessDenied() {
        ClientProfile p = ClientProfile.builder().id(9L).user(user(5L, Role.CLIENT)).build();
        when(repository.findById(9L)).thenReturn(Optional.of(p));

        assertThrows(AccessDeniedException.class, () -> service.getById(9L, new CurrentUser(6L, false)));
    }

    @Test
    void getById_admin_veAjeno() {
        ClientProfile p = ClientProfile.builder().id(9L).user(user(5L, Role.CLIENT)).build();
        when(repository.findById(9L)).thenReturn(Optional.of(p));

        assertEquals(9L, service.getById(9L, new CurrentUser(1L, true)).getId());
    }

    @Test
    void getById_inexistente_lanzaResourceNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(99L, new CurrentUser(5L, false)));
    }

    private static final CurrentUser ADMIN = new CurrentUser(1L, true);
    private static final CurrentUser OWNER = new CurrentUser(5L, false);

    @Test
    void update_admin_actualizaCampos() {
        ClientProfile existing = ClientProfile.builder().id(9L).user(user(5L, Role.CLIENT)).build();
        when(repository.findById(9L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        ClientProfileResponse response = service.update(9L, request(5L), ADMIN);

        assertEquals(new BigDecimal("175.0"), response.getInitialHeight());
        assertEquals(LocalDate.of(1990, 5, 10), response.getBirthDate());
    }

    @Test
    void update_propio_actualizaCampos() {
        ClientProfile existing = ClientProfile.builder().id(9L).user(user(5L, Role.CLIENT)).build();
        when(repository.findById(9L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        ClientProfileResponse response = service.update(9L, request(5L), OWNER);

        assertEquals(new BigDecimal("70.5"), response.getInitialWeight());
    }

    @Test
    void update_ajeno_lanzaAccessDenied() {
        ClientProfile existing = ClientProfile.builder().id(9L).user(user(5L, Role.CLIENT)).build();
        when(repository.findById(9L)).thenReturn(Optional.of(existing));

        assertThrows(AccessDeniedException.class,
                () -> service.update(9L, request(5L), new CurrentUser(6L, false)));
        verify(repository, never()).save(any());
    }

    @Test
    void update_noAdminCambiaUserId_lanzaIllegalArgument() {
        ClientProfile existing = ClientProfile.builder().id(9L).user(user(5L, Role.CLIENT)).build();
        when(repository.findById(9L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> service.update(9L, request(6L), OWNER));
        verify(repository, never()).save(any());
    }

    @Test
    void update_adminCambiaUserId_lanzaIllegalArgument() {
        ClientProfile existing = ClientProfile.builder().id(9L).user(user(5L, Role.CLIENT)).build();
        when(repository.findById(9L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> service.update(9L, request(6L), ADMIN));
        verify(repository, never()).save(any());
    }

    @Test
    void update_perfilNoExiste_lanzaResourceNotFound() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.update(9L, request(5L), OWNER));
        assertEquals("Client profile not found", ex.getMessage());
    }
}
