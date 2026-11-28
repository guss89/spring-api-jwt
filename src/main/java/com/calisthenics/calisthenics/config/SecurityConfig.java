package com.calisthenics.calisthenics.config;


import com.calisthenics.calisthenics.security.JsonAccessDeniedHandler;
import com.calisthenics.calisthenics.security.JsonAuthenticationEntryPoint;
import org.springframework.http.HttpMethod;
import com.calisthenics.calisthenics.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private static final HttpMethod[] WRITE_METHODS = {HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE};

    private static final String[] BILLING_PATHS = {
            "/api/v1/client-payments/**", "/api/v1/client-memberships/**"
    };

    private static final String CLIENT_ROUTINE_PATH = "/api/v1/client-routine/**";

    private static final String CLIENTS_PATH = "/api/v1/clients/**";

    private static final String COACH_CLIENTS_PATH = "/api/v1/coach-clients/**";

    private static final String[] CATALOG_PATHS = {
            "/api/v1/body-regions/**", "/api/v1/exercise-category/**", "/api/v1/type-routine/**",
            "/api/v1/exercises/**", "/api/v1/routines/**", "/api/v1/routine-exercise/**",
            "/api/v1/membership-plans/**"
    };

    private static final String[] ADMIN_WRITE_PATHS = {
            "/api/v1/evaluations/**"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter){
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {

        http
                .cors(Customizer.withDefaults())

                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new JsonAuthenticationEntryPoint(objectMapper))
                        .accessDeniedHandler(new JsonAccessDeniedHandler(objectMapper))
                )

                .authorizeHttpRequests(auth -> {
                        auth.requestMatchers("/auth/**", "/error").permitAll();
                        auth.requestMatchers("/api/v1/users/**").hasRole("ADMIN");
                        // Escritura: ADMIN en pagos y membresías; COACH en asignaciones de rutinas;
                        // ADMIN en catálogos y evaluaciones; perfiles de cliente: PUT para cualquier
                        // rol autenticado (el servicio exige que el perfil sea propio salvo ADMIN),
                        // POST y DELETE solo ADMIN
                        auth.requestMatchers(HttpMethod.PUT, CLIENTS_PATH).hasAnyRole("ADMIN", "CLIENT", "COACH");
                        for (HttpMethod method : WRITE_METHODS) {
                            auth.requestMatchers(method, BILLING_PATHS).hasRole("ADMIN");
                            auth.requestMatchers(method, CLIENT_ROUTINE_PATH).hasRole("COACH");
                            auth.requestMatchers(method, CLIENTS_PATH).hasRole("ADMIN");
                            auth.requestMatchers(method, COACH_CLIENTS_PATH).hasRole("ADMIN");
                            auth.requestMatchers(method, CATALOG_PATHS).hasRole("ADMIN");
                            auth.requestMatchers(method, ADMIN_WRITE_PATHS).hasRole("ADMIN");
                        }
                        auth.anyRequest().authenticated();
                })

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {

        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
