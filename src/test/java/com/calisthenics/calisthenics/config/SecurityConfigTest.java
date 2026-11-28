package com.calisthenics.calisthenics.config;

import com.calisthenics.calisthenics.controller.BodyRegionController;
import com.calisthenics.calisthenics.controller.ClientController;
import com.calisthenics.calisthenics.controller.ClientMembershipController;
import com.calisthenics.calisthenics.controller.ClientPaymentController;
import com.calisthenics.calisthenics.controller.ClientRoutineController;
import com.calisthenics.calisthenics.controller.CoachClientController;
import com.calisthenics.calisthenics.controller.ExerciseCategoryController;
import com.calisthenics.calisthenics.controller.ExerciseController;
import com.calisthenics.calisthenics.controller.MembershipPlanController;
import com.calisthenics.calisthenics.controller.RoutineController;
import com.calisthenics.calisthenics.controller.RoutineExerciseController;
import com.calisthenics.calisthenics.controller.TypeRoutineController;
import com.calisthenics.calisthenics.repository.UserRepository;
import com.calisthenics.calisthenics.security.CustomUserDetailsService;
import com.calisthenics.calisthenics.service.BodyRegionService;
import com.calisthenics.calisthenics.service.ClientMembershipService;
import com.calisthenics.calisthenics.service.ClientProfileService;
import com.calisthenics.calisthenics.service.ClientPaymentService;
import com.calisthenics.calisthenics.service.ClientRoutineService;
import com.calisthenics.calisthenics.service.CoachClientService;
import com.calisthenics.calisthenics.service.ExerciseCategoryService;
import com.calisthenics.calisthenics.service.ExerciseService;
import com.calisthenics.calisthenics.service.JwtService;
import com.calisthenics.calisthenics.service.MembershipPlanService;
import com.calisthenics.calisthenics.service.RoutineExerciseService;
import com.calisthenics.calisthenics.service.RoutineService;
import com.calisthenics.calisthenics.service.TypeRoutineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Matriz de permisos: catálogos, pagos y membresías los escribe ADMIN; asignación de rutinas
 * la escribe COACH; PUT de clientes lo puede hacer cualquier rol (el servicio exige que sea propio);
 * GET abierto a autenticados.
 */
@WebMvcTest({BodyRegionController.class, ExerciseCategoryController.class, ExerciseController.class,
        RoutineController.class, RoutineExerciseController.class, MembershipPlanController.class,
        TypeRoutineController.class,
        ClientPaymentController.class, ClientMembershipController.class, ClientRoutineController.class,
        ClientController.class, CoachClientController.class})
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class})
@TestPropertySource(properties = "jwt.secret=test-secret-not-real-0123456789-abcdefghijkl")
class SecurityConfigTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;

    @MockBean private BodyRegionService bodyRegionService;
    @MockBean private ExerciseCategoryService exerciseCategoryService;
    @MockBean private ExerciseService exerciseService;
    @MockBean private RoutineService routineService;
    @MockBean private RoutineExerciseService routineExerciseService;
    @MockBean private MembershipPlanService membershipPlanService;
    @MockBean private TypeRoutineService typeRoutineService;
    @MockBean private ClientPaymentService clientPaymentService;
    @MockBean private ClientMembershipService clientMembershipService;
    @MockBean private ClientRoutineService clientRoutineService;
    @MockBean private ClientProfileService clientProfileService;
    @MockBean private CoachClientService coachClientService;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private UserRepository userRepository;

    private static final String[] CATALOGS = {
            "/api/v1/body-regions", "/api/v1/exercise-category", "/api/v1/type-routine", "/api/v1/exercises",
            "/api/v1/routines", "/api/v1/routine-exercise", "/api/v1/membership-plans"
    };
    private static final String[] BILLING = {"/api/v1/client-payments", "/api/v1/client-memberships"};
    private static final String CLIENT_ROUTINE = "/api/v1/client-routine";
    private static final String CLIENTS = "/api/v1/clients";
    private static final String CLIENT_BODY = "{\"userId\":1}";

    private String token(String role) {
        return "Bearer " + jwtService.generateToken(1L, role);
    }

    private MockHttpServletRequestBuilder write(HttpMethod method, String url, String role) {
        String path = method == HttpMethod.POST ? url : url + "/1";
        return request(method, path).header("Authorization", token(role))
                .contentType(MediaType.APPLICATION_JSON).content("{}");
    }

    private static final HttpMethod[] WRITES = {HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE};

    @Test
    void catalogos_escrituraConClientOCoach_403() throws Exception {
        for (String role : new String[]{"CLIENT", "COACH"}) {
            for (String url : CATALOGS) {
                for (HttpMethod m : WRITES) {
                    mockMvc.perform(write(m, url, role)).andExpect(status().isForbidden());
                }
            }
        }
    }

    @Test
    void catalogos_escrituraConAdmin_pasaLaSeguridad() throws Exception {
        for (String url : CATALOGS) {
            for (HttpMethod m : WRITES) {
                int status = mockMvc.perform(write(m, url, "ADMIN")).andReturn().getResponse().getStatus();
                assertThat(status).as(m + " " + url).isNotIn(401, 403);
            }
        }
    }

    @Test
    void catalogos_postVacioConAdmin_400() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/body-regions").header("Authorization", token("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void catalogos_getConClient_200() throws Exception {
        for (String url : CATALOGS) {
            mockMvc.perform(get(url).header("Authorization", token("CLIENT"))).andExpect(status().isOk());
        }
    }

    @Test
    void pagosYMembresias_escrituraConClientOCoach_403() throws Exception {
        for (String role : new String[]{"CLIENT", "COACH"}) {
            for (String url : BILLING) {
                for (HttpMethod m : WRITES) {
                    mockMvc.perform(write(m, url, role)).andExpect(status().isForbidden());
                }
            }
        }
    }

    @Test
    void pagosYMembresias_escrituraConAdmin_pasaLaSeguridad() throws Exception {
        for (String url : BILLING) {
            for (HttpMethod m : WRITES) {
                int status = mockMvc.perform(write(m, url, "ADMIN")).andReturn().getResponse().getStatus();
                assertThat(status).as(m + " " + url).isNotIn(401, 403);
            }
        }
    }

    @Test
    void pagosYMembresias_postVacioConAdmin_400Json() throws Exception {
        for (String url : BILLING) {
            mockMvc.perform(write(HttpMethod.POST, url, "ADMIN"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors").exists());
        }
    }

    @Test
    void asignacionRutinas_escrituraConAdminOClient_403() throws Exception {
        for (String role : new String[]{"ADMIN", "CLIENT"}) {
            for (HttpMethod m : WRITES) {
                mockMvc.perform(write(m, CLIENT_ROUTINE, role)).andExpect(status().isForbidden());
            }
        }
    }

    @Test
    void asignacionRutinas_escrituraConCoach_pasaLaSeguridad() throws Exception {
        for (HttpMethod m : WRITES) {
            int status = mockMvc.perform(write(m, CLIENT_ROUTINE, "COACH")).andReturn().getResponse().getStatus();
            assertThat(status).as(m.toString()).isNotIn(401, 403);
        }
    }

    @Test
    void asignacionRutinas_lecturaConCualquierRol_200() throws Exception {
        for (String role : new String[]{"ADMIN", "CLIENT", "COACH"}) {
            mockMvc.perform(get(CLIENT_ROUTINE).header("Authorization", token(role))).andExpect(status().isOk());
        }
    }

    @Test
    void clientes_putPermitidoAClientCoachYAdmin_postYDeleteSoloAdmin() throws Exception {
        for (String role : new String[]{"CLIENT", "COACH", "ADMIN"}) {
            mockMvc.perform(write(HttpMethod.PUT, CLIENTS, role).content(CLIENT_BODY))
                    .andExpect(status().isOk());
        }
        for (String role : new String[]{"CLIENT", "COACH"}) {
            mockMvc.perform(write(HttpMethod.POST, CLIENTS, role).content(CLIENT_BODY))
                    .andExpect(status().isForbidden());
            mockMvc.perform(write(HttpMethod.DELETE, CLIENTS, role)).andExpect(status().isForbidden());
        }
        int post = mockMvc.perform(write(HttpMethod.POST, CLIENTS, "ADMIN").content(CLIENT_BODY))
                .andReturn().getResponse().getStatus();
        assertThat(post).isNotIn(401, 403);
        int delete = mockMvc.perform(write(HttpMethod.DELETE, CLIENTS, "ADMIN"))
                .andReturn().getResponse().getStatus();
        assertThat(delete).isNotIn(401, 403);
    }

    @Test
    void coachClients_postYDeleteSoloAdmin() throws Exception {
        String body = "{\"coachId\":2,\"clientId\":3}";
        for (String role : new String[]{"CLIENT", "COACH"}) {
            mockMvc.perform(write(HttpMethod.POST, "/api/v1/coach-clients", role).content(body))
                    .andExpect(status().isForbidden());
            mockMvc.perform(write(HttpMethod.DELETE, "/api/v1/coach-clients", role))
                    .andExpect(status().isForbidden());
        }
        mockMvc.perform(write(HttpMethod.POST, "/api/v1/coach-clients", "ADMIN").content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(write(HttpMethod.DELETE, "/api/v1/coach-clients", "ADMIN"))
                .andExpect(status().isNoContent());
    }

    @Test
    void coachClients_lecturaAbiertaAAutenticados() throws Exception {
        for (String role : new String[]{"ADMIN", "CLIENT", "COACH"}) {
            mockMvc.perform(get("/api/v1/coach-clients").param("coachId", "1")
                            .header("Authorization", token(role)))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void clientes_putAjeno_403() throws Exception {
        when(clientProfileService.update(eq(1L), any(), any())).thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(write(HttpMethod.PUT, CLIENTS, "CLIENT").content(CLIENT_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void denegacion_devuelveErrorResponseJson() throws Exception {
        mockMvc.perform(write(HttpMethod.POST, "/api/v1/client-payments", "CLIENT"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void sinToken_sigueSiendo401() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/body-regions")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
