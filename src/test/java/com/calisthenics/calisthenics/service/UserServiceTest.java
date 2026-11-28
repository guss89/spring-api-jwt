package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.UserRequest;
import com.calisthenics.calisthenics.dto.UserResponse;
import com.calisthenics.calisthenics.dto.UserUpdateRequest;
import com.calisthenics.calisthenics.exception.ConflictException;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.model.catalogs.Role;
import com.calisthenics.calisthenics.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository repository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(repository, passwordEncoder);
    }

    private User user(Long id, String username, String email, Role role) {
        return User.builder().id(id).name("N").username(username).email(email)
                .passwordHash("old-hash").role(role).active(true).build();
    }

    private UserRequest createRequest() {
        UserRequest r = new UserRequest();
        r.setName("Ana");
        r.setUsername("ana1");
        r.setEmail("ana@x.com");
        r.setPassword("password123");
        r.setRole(Role.COACH);
        return r;
    }

    private UserUpdateRequest updateRequest(Role role) {
        UserUpdateRequest r = new UserUpdateRequest();
        r.setName("Ana");
        r.setUsername("ana1");
        r.setEmail("ana@x.com");
        r.setRole(role);
        return r;
    }

    @Test
    void create_guardaHashYActivoPorDefecto() {
        when(repository.findByUsername("ana1")).thenReturn(Optional.empty());
        when(repository.findByEmail("ana@x.com")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse response = service.create(createRequest());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());
        User saved = captor.getValue();
        assertNotEquals("password123", saved.getPasswordHash());
        assertTrue(passwordEncoder.matches("password123", saved.getPasswordHash()));
        assertEquals(Role.COACH, saved.getRole());
        assertTrue(saved.getActive());
        assertEquals(Role.COACH, response.getRole());
    }

    @Test
    void create_usernameDuplicado_lanzaConflict() {
        when(repository.findByUsername("ana1")).thenReturn(Optional.of(user(9L, "ana1", "o@x.com", Role.CLIENT)));

        assertThrows(ConflictException.class, () -> service.create(createRequest()));
        verify(repository, never()).save(any());
    }

    @Test
    void create_emailDuplicado_lanzaConflict() {
        when(repository.findByUsername("ana1")).thenReturn(Optional.empty());
        when(repository.findByEmail("ana@x.com")).thenReturn(Optional.of(user(9L, "otro", "ana@x.com", Role.CLIENT)));

        assertThrows(ConflictException.class, () -> service.create(createRequest()));
        verify(repository, never()).save(any());
    }

    @Test
    void getAll_devuelveDtos() {
        when(repository.findAll()).thenReturn(List.of(user(1L, "a", "a@x.com", Role.ADMIN)));

        assertEquals(1, service.getAll().size());
    }

    @Test
    void getById_noExiste_lanza404() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(5L));
    }

    @Test
    void update_noExiste_lanza404() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(5L, updateRequest(Role.CLIENT), 1L));
    }

    @Test
    void update_sinPassword_noTocaElHash() {
        User existing = user(5L, "viejo", "viejo@x.com", Role.CLIENT);
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.findByUsername("ana1")).thenReturn(Optional.empty());
        when(repository.findByEmail("ana@x.com")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        service.update(5L, updateRequest(Role.COACH), 1L);

        assertEquals("old-hash", existing.getPasswordHash());
        assertEquals("ana1", existing.getUsername());
        assertEquals(Role.COACH, existing.getRole());
        assertTrue(existing.getActive());
    }

    @Test
    void update_conPassword_reHashea() {
        User existing = user(5L, "viejo", "viejo@x.com", Role.CLIENT);
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.findByUsername("ana1")).thenReturn(Optional.empty());
        when(repository.findByEmail("ana@x.com")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        UserUpdateRequest request = updateRequest(Role.CLIENT);
        request.setPassword("nuevaClave123");

        service.update(5L, request, 1L);

        assertTrue(passwordEncoder.matches("nuevaClave123", existing.getPasswordHash()));
    }

    @Test
    void update_usernameDeOtroUsuario_lanzaConflict() {
        when(repository.findById(5L)).thenReturn(Optional.of(user(5L, "viejo", "viejo@x.com", Role.CLIENT)));
        when(repository.findByUsername("ana1")).thenReturn(Optional.of(user(9L, "ana1", "o@x.com", Role.CLIENT)));

        assertThrows(ConflictException.class, () -> service.update(5L, updateRequest(Role.CLIENT), 1L));
        verify(repository, never()).save(any());
    }

    @Test
    void update_emailDeOtroUsuario_lanzaConflict() {
        when(repository.findById(5L)).thenReturn(Optional.of(user(5L, "viejo", "viejo@x.com", Role.CLIENT)));
        when(repository.findByUsername("ana1")).thenReturn(Optional.empty());
        when(repository.findByEmail("ana@x.com")).thenReturn(Optional.of(user(9L, "otro", "ana@x.com", Role.CLIENT)));

        assertThrows(ConflictException.class, () -> service.update(5L, updateRequest(Role.CLIENT), 1L));
        verify(repository, never()).save(any());
    }

    @Test
    void update_mismoUsernameDelPropioUsuario_noEsConflicto() {
        User existing = user(5L, "ana1", "ana@x.com", Role.CLIENT);
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.findByUsername("ana1")).thenReturn(Optional.of(existing));
        when(repository.findByEmail("ana@x.com")).thenReturn(Optional.of(existing));
        when(repository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> service.update(5L, updateRequest(Role.CLIENT), 1L));
    }

    @Test
    void update_adminQuitaseSuRol_lanza400() {
        when(repository.findById(1L)).thenReturn(Optional.of(user(1L, "adm", "adm@x.com", Role.ADMIN)));

        assertThrows(IllegalArgumentException.class, () -> service.update(1L, updateRequest(Role.COACH), 1L));
        verify(repository, never()).save(any());
    }

    @Test
    void update_adminSeDesactiva_lanza400() {
        when(repository.findById(1L)).thenReturn(Optional.of(user(1L, "adm", "adm@x.com", Role.ADMIN)));
        UserUpdateRequest request = updateRequest(Role.ADMIN);
        request.setActive(false);

        assertThrows(IllegalArgumentException.class, () -> service.update(1L, request, 1L));
        verify(repository, never()).save(any());
    }

    @Test
    void update_adminSeEditaConRolAdminYActiveNull_ok() {
        User existing = user(1L, "adm", "adm@x.com", Role.ADMIN);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByUsername("ana1")).thenReturn(Optional.empty());
        when(repository.findByEmail("ana@x.com")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> service.update(1L, updateRequest(Role.ADMIN), 1L));
        assertTrue(existing.getActive());
    }

    @Test
    void update_adminSeEditaConActiveTrue_ok() {
        User existing = user(1L, "adm", "adm@x.com", Role.ADMIN);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByUsername("ana1")).thenReturn(Optional.empty());
        when(repository.findByEmail("ana@x.com")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        UserUpdateRequest request = updateRequest(Role.ADMIN);
        request.setActive(true);

        assertDoesNotThrow(() -> service.update(1L, request, 1L));
        assertTrue(existing.getActive());
    }

    @Test
    void update_adminEditaAOtroAdmin_ok() {
        User other = user(2L, "adm2", "adm2@x.com", Role.ADMIN);
        when(repository.findById(2L)).thenReturn(Optional.of(other));
        when(repository.findByUsername("ana1")).thenReturn(Optional.empty());
        when(repository.findByEmail("ana@x.com")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        UserUpdateRequest request = updateRequest(Role.CLIENT);
        request.setActive(false);

        assertDoesNotThrow(() -> service.update(2L, request, 1L));
        assertEquals(Role.CLIENT, other.getRole());
        assertFalse(other.getActive());
    }

    @Test
    void update_usernameYEmailNulos_noConsultaDuplicados() {
        User existing = user(5L, "viejo", "viejo@x.com", Role.CLIENT);
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        UserUpdateRequest request = updateRequest(Role.CLIENT);
        request.setUsername(null);
        request.setEmail("  ");

        service.update(5L, request, 1L);

        verify(repository, never()).findByUsername(any());
        verify(repository, never()).findByEmail(any());
    }

    @Test
    void deactivate_ponActiveEnFalse() {
        User existing = user(5L, "u", "u@x.com", Role.CLIENT);
        when(repository.findById(5L)).thenReturn(Optional.of(existing));

        service.deactivate(5L, 1L);

        assertFalse(existing.getActive());
        verify(repository).save(existing);
        verify(repository, never()).delete(any());
    }

    @Test
    void deactivate_noExiste_lanza404() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deactivate(5L, 1L));
    }

    @Test
    void deactivate_adminASiMismo_lanza400() {
        when(repository.findById(1L)).thenReturn(Optional.of(user(1L, "adm", "adm@x.com", Role.ADMIN)));

        assertThrows(IllegalArgumentException.class, () -> service.deactivate(1L, 1L));
        verify(repository, never()).save(any());
    }
}
