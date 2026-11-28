package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.*;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.*;
import com.calisthenics.calisthenics.repository.*;
import org.junit.jupiter.api.Test;
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
class RoutineExerciseServiceTest {

    @Mock private RoutineRepository routineRepository;
    @Mock private ExerciseRepository exerciseRepository;
    @Mock private BodyRegionRepository bodyRegionRepository;
    @Mock private RoutineExerciseRepository routineExerciseRepository;
    @InjectMocks private RoutineExerciseService service;

    private RoutineExerciseRequest request() {
        RoutineExerciseRequest r = new RoutineExerciseRequest();
        r.setReps(10);
        r.setRestSeconds(60);
        r.setSets(3);
        r.setBodyRegionId(1L);
        r.setExerciseId(2L);
        r.setRoutineId(3L);
        return r;
    }

    private Routine routine() { Routine r = new Routine(); r.setId(3L); return r; }
    private Exercise exercise() { Exercise e = new Exercise(); e.setId(2L); return e; }
    private BodyRegion region() { BodyRegion b = new BodyRegion(); b.setId(1L); return b; }

    private RoutineExercise existing() {
        RoutineExercise re = new RoutineExercise();
        re.setId(1L);
        re.setReps(5);
        re.setSets(1);
        re.setRestSeconds(30);
        re.setRoutine(routine());
        re.setExercise(exercise());
        re.setBodyRegion(region());
        return re;
    }

    @Test
    void getAll_devuelveDtos() {
        when(routineExerciseRepository.findAll()).thenReturn(List.of(existing()));

        List<RoutineExerciseResponse> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getRoutineId());
        assertEquals(2L, result.get(0).getExerciseId());
        assertEquals(1L, result.get(0).getBodyRegionId());
    }

    @Test
    void save_guardaYDevuelveDto() {
        when(routineRepository.findById(3L)).thenReturn(Optional.of(routine()));
        when(exerciseRepository.findById(2L)).thenReturn(Optional.of(exercise()));
        when(bodyRegionRepository.findById(1L)).thenReturn(Optional.of(region()));
        when(routineExerciseRepository.save(any(RoutineExercise.class))).thenAnswer(inv -> {
            RoutineExercise re = inv.getArgument(0);
            re.setId(7L);
            return re;
        });

        RoutineExerciseResponse result = service.save(request());

        assertEquals(7L, result.getId());
        assertEquals(10, result.getReps());
    }

    @Test
    void update_devuelveDto() {
        RoutineExercise ex = existing();
        when(routineExerciseRepository.findById(1L)).thenReturn(Optional.of(ex));
        when(bodyRegionRepository.findById(1L)).thenReturn(Optional.of(region()));
        when(exerciseRepository.findById(2L)).thenReturn(Optional.of(exercise()));
        when(routineRepository.findById(3L)).thenReturn(Optional.of(routine()));
        when(routineExerciseRepository.save(ex)).thenReturn(ex);

        RoutineExerciseResponse result = service.update(1L, request());

        assertEquals(10, result.getReps());
        assertEquals(3, result.getSets());
    }

    @Test
    void update_inexistente_lanzaResourceNotFound() {
        when(routineExerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(99L, request()));
    }
}
