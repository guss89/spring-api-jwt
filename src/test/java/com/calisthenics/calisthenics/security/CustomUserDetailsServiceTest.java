package com.calisthenics.calisthenics.security;

import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.model.catalogs.Role;
import com.calisthenics.calisthenics.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private CustomUserDetailsService service;

    private User user(Boolean active) {
        return User.builder().id(1L).email("a@b.com").passwordHash("hash")
                .role(Role.CLIENT).active(active).build();
    }

    @Test
    void usuarioActivo_estaHabilitado() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user(true)));

        UserDetails details = service.loadUserByUsername("a@b.com");

        assertTrue(details.isEnabled());
    }

    @Test
    void usuarioInactivo_estaDeshabilitado() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user(false)));

        UserDetails details = service.loadUserByUsername("a@b.com");

        assertFalse(details.isEnabled());
    }

    @Test
    void usuarioActiveNull_estaDeshabilitado() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user(null)));

        UserDetails details = service.loadUserByUsername("a@b.com");

        assertFalse(details.isEnabled());
    }
}
