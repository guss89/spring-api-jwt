package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.config.CorsConfig;
import com.calisthenics.calisthenics.config.SecurityConfig;
import com.calisthenics.calisthenics.dto.LoginResponse;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.model.catalogs.Role;
import com.calisthenics.calisthenics.repository.UserRepository;
import com.calisthenics.calisthenics.security.CustomUserDetailsService;
import com.calisthenics.calisthenics.service.AuthService;
import com.calisthenics.calisthenics.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class})
@TestPropertySource(properties = "jwt.secret=test-secret-not-real-0123456789-abcdefghijkl")
class AuthControllerTest {

    private static final String LOGIN_BODY = """
            {"email":"a@b.com","password":"12345678"}
            """;
    private static final String REGISTER_BODY = """
            {"name":"Ana","lastName":"Perez","username":"anap","email":"a@b.com",
             "phone":"555","password":"12345678","role":"CLIENT"}
            """;

    @Autowired private MockMvc mockMvc;
    @MockBean private AuthService authService;
    @Autowired private JwtService jwtService;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private UserRepository userRepository;

    @Test
    void login_devuelveJsonConTokenRolYUserId() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse("tok123", "CLIENT", 7L));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("tok123"))
                .andExpect(jsonPath("$.role").value("CLIENT"))
                .andExpect(jsonPath("$.userId").value(7));
    }

    @Test
    void login_usuarioDesactivado_devuelve401ConJsonYSinToken() throws Exception {
        when(authService.login(any()))
                .thenThrow(new org.springframework.security.authentication.DisabledException("User is disabled"));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void register_noExponePasswordHash() throws Exception {
        User user = User.builder().id(3L).name("Ana").lastName("Perez").username("anap")
                .email("a@b.com").phone("555").passwordHash("$2a$secreto").role(Role.CLIENT)
                .active(true).build();
        when(authService.register(any())).thenReturn(user);

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.email").value("a@b.com"))
                .andExpect(jsonPath("$.role").value("CLIENT"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("secreto"))));
    }

    @Test
    void register_duplicado_devuelve409ConJson() throws Exception {
        when(authService.register(any()))
                .thenThrow(new com.calisthenics.calisthenics.exception.ConflictException("Email already registered"));

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER_BODY))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    void register_usernameDe30Caracteres_pasaValidacion() throws Exception {
        when(authService.register(any())).thenReturn(User.builder().id(3L).username("a".repeat(30))
                .email("a@b.com").role(Role.CLIENT).active(true).build());

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerBodyWithUsername("a".repeat(30))))
                .andExpect(status().isOk());
    }

    @Test
    void register_usernameDe51Caracteres_devuelve400ConErrorEnUsername() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerBodyWithUsername("a".repeat(51))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.username").exists());
    }

    @Test
    void register_usernameDe50Caracteres_pasaValidacion() throws Exception {
        when(authService.register(any())).thenReturn(User.builder().id(3L).username("a".repeat(50))
                .email("a@b.com").role(Role.CLIENT).active(true).build());

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerBodyWithUsername("a".repeat(50))))
                .andExpect(status().isOk());
    }

    @Test
    void register_usernameDe3Caracteres_devuelve400ConErrorEnUsername() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerBodyWithUsername("abc")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists());
    }

    @Test
    void register_emailDe101Caracteres_devuelve400ConErrorEnEmail() throws Exception {
        String email = "a".repeat(89) + "@example.com";
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("anap", email, "12345678")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void register_passwordDe7Caracteres_devuelve400ConErrorEnPassword() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("anap", "a@b.com", "1234567")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void register_passwordDe73Caracteres_devuelve400ConErrorEnPassword() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("anap", "a@b.com", "a".repeat(73))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    private static String registerBodyWithUsername(String username) {
        return registerBody(username, "a@b.com", "12345678");
    }

    private static String registerBody(String username, String email, String password) {
        return "{\"name\":\"Ana\",\"lastName\":\"Perez\",\"username\":\"" + username
                + "\",\"email\":\"" + email + "\",\"phone\":\"555\",\"password\":\"" + password + "\"}";
    }

    @Test
    void rutaProtegidaSinToken_devuelve401ConJson() throws Exception {
        mockMvc.perform(get("/api/v1/client-payments"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void preflightDesdeOrigenPermitido_incluyeAllowOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/client-payments/1")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "PUT")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Headers",
                        org.hamcrest.Matchers.containsString("Authorization")));
    }

    @Test
    void preflightDesdeOrigenNoPermitido_noIncluyeAllowOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/client-payments/1")
                        .header("Origin", "http://evil.example.com")
                        .header("Access-Control-Request-Method", "PUT")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void rutaProtegidaConTokenInvalido_devuelve401ConJson() throws Exception {
        mockMvc.perform(get("/api/v1/client-payments")
                        .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void registerConTokenValidoYJsonInvalido_devuelve400() throws Exception {
        String token = jwtService.generateToken(1L, "ADMIN");

        mockMvc.perform(post("/auth/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{json mal formado"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginConTokenValidoYBodyInvalido_devuelve400() throws Exception {
        String token = jwtService.generateToken(1L, "ADMIN");

        mockMvc.perform(post("/auth/login")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"password\":\"corta\"}"))
                .andExpect(status().isBadRequest());
    }
}
