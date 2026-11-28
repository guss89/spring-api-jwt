package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.EvaluationRequest;
import com.calisthenics.calisthenics.dto.EvaluationResponse;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.CoachClient;
import com.calisthenics.calisthenics.model.Evaluation;
import com.calisthenics.calisthenics.repository.CoachClientRepository;
import com.calisthenics.calisthenics.model.Exercise;
import com.calisthenics.calisthenics.model.User;
import com.calisthenics.calisthenics.repository.EvaluationRepository;
import com.calisthenics.calisthenics.repository.ExerciseRepository;
import com.calisthenics.calisthenics.repository.UserRepository;
import com.calisthenics.calisthenics.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock private ExerciseRepository exerciseRepository;
    @Mock private EvaluationRepository evaluationRepository;
    @Mock private UserRepository userRepository;
    @Mock private CoachClientRepository coachClientRepository;
    @InjectMocks private EvaluationService service;

    private Evaluation evaluation(Long ownerId) {
        User u = new User();
        u.setId(ownerId);
        Exercise e = new Exercise();
        e.setId(3L);
        Evaluation ev = new Evaluation();
        ev.setId(1L);
        ev.setUser(u);
        ev.setExercise(e);
        ev.setRegisterDate(LocalDate.of(2026, 1, 1));
        return ev;
    }

    private EvaluationRequest request() {
        EvaluationRequest r = new EvaluationRequest();
        r.setUserId(5L);
        r.setExerciseId(3L);
        r.setMaxReps(10L);
        r.setRegisterDate(LocalDate.of(2026, 1, 1));
        return r;
    }

    private CoachClient assignment(Long clientId) {
        User c = new User();
        c.setId(clientId);
        CoachClient cc = new CoachClient();
        cc.setClient(c);
        return cc;
    }

    private static final CurrentUser COACH = new CurrentUser(10L, false, true);

    @Test
    void getAll_admin_devuelveTodas() {
        when(evaluationRepository.findAll()).thenReturn(List.of(evaluation(5L)));

        assertEquals(1, service.getAll(null, new CurrentUser(1L, true)).size());
    }

    @Test
    void getAll_adminConFiltro_soloEseUsuario() {
        when(evaluationRepository.findByUserId(5L)).thenReturn(List.of(evaluation(5L)));

        assertEquals(1, service.getAll(5L, new CurrentUser(1L, true)).size());
        verify(evaluationRepository, never()).findAll();
    }

    @Test
    void getAll_noAdmin_soloLasSuyas() {
        when(evaluationRepository.findByUserId(5L)).thenReturn(List.of(evaluation(5L)));

        List<EvaluationResponse> result = service.getAll(null, new CurrentUser(5L, false, false, true));

        assertEquals(5L, result.get(0).getUserId());
        assertEquals(3L, result.get(0).getExerciseId());
        verify(evaluationRepository, never()).findAll();
    }

    @Test
    void getAll_clientConUserIdAjeno_lanzaAccessDenied() {
        assertThrows(AccessDeniedException.class,
                () -> service.getAll(6L, new CurrentUser(5L, false, false, true)));
        verifyNoInteractions(evaluationRepository);
    }

    @Test
    void getAll_clientConSuUserId_devuelveLasSuyas() {
        when(evaluationRepository.findByUserId(5L)).thenReturn(List.of(evaluation(5L)));

        assertEquals(1, service.getAll(5L, new CurrentUser(5L, false, false, true)).size());
    }

    @Test
    void getAll_coachConClienteAsignado_usaRelacionYNoFindAll() {
        when(coachClientRepository.existsByCoachIdAndClientId(10L, 5L)).thenReturn(true);
        when(evaluationRepository.findByUserId(5L)).thenReturn(List.of(evaluation(5L)));

        List<EvaluationResponse> result = service.getAll(5L, COACH);

        assertEquals(1, result.size());
        verify(evaluationRepository, never()).findAll();
    }

    @Test
    void getAll_coachConClienteNoAsignado_lanzaAccessDenied() {
        when(coachClientRepository.existsByCoachIdAndClientId(10L, 7L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.getAll(7L, COACH));
        verifyNoInteractions(evaluationRepository);
    }

    @Test
    void getAll_coachSeUsaASiMismoComoUserId_lanzaAccessDenied() {
        when(coachClientRepository.existsByCoachIdAndClientId(10L, 10L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.getAll(10L, COACH));
    }

    @Test
    void getAll_coachSinFiltro_soloEvaluacionesDeSusClientes() {
        when(coachClientRepository.findByCoachId(10L)).thenReturn(List.of(assignment(5L), assignment(6L)));
        when(evaluationRepository.findByUserIdIn(List.of(5L, 6L)))
                .thenReturn(List.of(evaluation(5L), evaluation(6L)));

        assertEquals(2, service.getAll(null, COACH).size());
        verify(evaluationRepository, never()).findAll();
    }

    @Test
    void getAll_coachSinClientes_devuelveVacio() {
        when(coachClientRepository.findByCoachId(10L)).thenReturn(List.of());

        assertTrue(service.getAll(null, COACH).isEmpty());
        verifyNoInteractions(evaluationRepository);
    }

    @Test
    void getById_coachConClienteAsignado_devuelveResponse() {
        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(evaluation(5L)));
        when(coachClientRepository.existsByCoachIdAndClientId(10L, 5L)).thenReturn(true);

        assertEquals(1L, service.getById(1L, COACH).getId());
    }

    @Test
    void getById_coachConClienteNoAsignado_lanzaAccessDenied() {
        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(evaluation(5L)));
        when(coachClientRepository.existsByCoachIdAndClientId(10L, 5L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.getById(1L, COACH));
    }

    @Test
    void getById_coachInexistente_lanzaResourceNotFoundAntesQueAccessDenied() {
        when(evaluationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(99L, COACH));
        verifyNoInteractions(coachClientRepository);
    }

    @Test
    void getById_ajena_lanzaAccessDenied() {
        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(evaluation(5L)));

        assertThrows(AccessDeniedException.class, () -> service.getById(1L, new CurrentUser(6L, false)));
    }

    @Test
    void getById_propiaYAdmin_devuelveResponse() {
        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(evaluation(5L)));

        assertEquals(1L, service.getById(1L, new CurrentUser(5L, false)).getId());
        assertEquals(1L, service.getById(1L, new CurrentUser(1L, true)).getId());
    }

    @Test
    void getById_inexistente_lanzaResourceNotFound() {
        when(evaluationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(99L, new CurrentUser(1L, true)));
    }

    @Test
    void update_guardaYDevuelveDto() {
        Evaluation existing = evaluation(5L);
        User u = new User();
        u.setId(5L);
        Exercise e = new Exercise();
        e.setId(3L);
        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(5L)).thenReturn(Optional.of(u));
        when(exerciseRepository.findById(3L)).thenReturn(Optional.of(e));
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(inv -> inv.getArgument(0));

        EvaluationResponse result = service.update(1L, request());

        assertEquals(10L, result.getMaxReps());
        assertEquals(5L, result.getUserId());
        assertEquals(3L, result.getExerciseId());
    }
}
