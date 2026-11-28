package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.LoginRequest;
import com.calisthenics.calisthenics.dto.LoginResponse;
import com.calisthenics.calisthenics.dto.RegisterRequest;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.model.catalogs.Role;
import com.calisthenics.calisthenics.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @InjectMocks private AuthService authService;

    @Test
    void login_devuelveTokenRolYUserId() throws Exception {
        User user = User.builder().id(9L).email("a@b.com").role(Role.COACH).build();
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(9L, "COACH")).thenReturn("tok");

        LoginRequest request = new ObjectMapper()
                .readValue("{\"email\":\"a@b.com\",\"password\":\"12345678\"}", LoginRequest.class);

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("tok");
        assertThat(response.getRole()).isEqualTo("COACH");
        assertThat(response.getUserId()).isEqualTo(9L);
    }

    @Test
    void login_credencialesInvalidas_propagaExcepcionYNoGeneraToken() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        LoginRequest request = new ObjectMapper()
                .readValue("{\"email\":\"a@b.com\",\"password\":\"12345678\"}", LoginRequest.class);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
        verify(jwtService, never()).generateToken(anyLong(), anyString());
    }

    @Test
    void login_usuarioDesactivado_propagaDisabledExceptionYNoGeneraToken() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new org.springframework.security.authentication.DisabledException("User is disabled"));

        LoginRequest request = new ObjectMapper()
                .readValue("{\"email\":\"a@b.com\",\"password\":\"12345678\"}", LoginRequest.class);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(org.springframework.security.authentication.DisabledException.class);
        verify(jwtService, never()).generateToken(anyLong(), anyString());
    }

    @Test
    void register_devuelveUsuarioGuardadoConHashYNoLaPasswordEnClaro() throws Exception {
        RegisterRequest request = new ObjectMapper().readValue(
                "{\"name\":\"Ana\",\"lastName\":\"Perez\",\"username\":\"anap\",\"email\":\"a@b.com\","
                        + "\"phone\":\"555\",\"password\":\"12345678\",\"role\":\"CLIENT\"}",
                RegisterRequest.class);
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("anap")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("12345678")).thenReturn("$hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = authService.register(request);

        assertThat(saved.getPasswordHash()).isEqualTo("$hash").isNotEqualTo("12345678");
        assertThat(saved.getEmail()).isEqualTo("a@b.com");
        assertThat(saved.getRole()).isEqualTo(Role.CLIENT);
        assertThat(saved.getActive()).isTrue();
    }

    @Test
    void register_usernameDuplicado_lanzaConflictYNoGuarda() throws Exception {
        RegisterRequest request = registerRequest();
        when(userRepository.findByUsername("anap")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(com.calisthenics.calisthenics.exception.ConflictException.class)
                .hasMessage("Username already exists");
        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_emailDuplicado_lanzaConflictYNoGuarda() throws Exception {
        RegisterRequest request = registerRequest();
        when(userRepository.findByUsername("anap")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(com.calisthenics.calisthenics.exception.ConflictException.class)
                .hasMessage("Email already registered");
        verify(userRepository, never()).save(any(User.class));
    }

    private RegisterRequest registerRequest() throws Exception {
        return new ObjectMapper().readValue(
                "{\"name\":\"Ana\",\"lastName\":\"Perez\",\"username\":\"anap\",\"email\":\"a@b.com\","
                        + "\"phone\":\"555\",\"password\":\"12345678\"}",
                RegisterRequest.class);
    }

    @Test
    void register_ignoraElRolEnviadoYForzaClient() throws Exception {
        RegisterRequest request = new ObjectMapper().readValue(
                "{\"name\":\"Ana\",\"lastName\":\"Perez\",\"username\":\"anap\",\"email\":\"a@b.com\","
                        + "\"phone\":\"555\",\"password\":\"12345678\",\"role\":\"ADMIN\"}",
                RegisterRequest.class);
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("anap")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("12345678")).thenReturn("$hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(authService.register(request).getRole()).isEqualTo(Role.CLIENT);
    }
}
