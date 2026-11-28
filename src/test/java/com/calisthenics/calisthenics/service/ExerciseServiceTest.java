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
class ExerciseServiceTest {

    @Mock private ExerciseCategoryRepository exerciseCategoryRepository;
    @Mock private ExerciseRepository exerciseRepository;
    @InjectMocks private ExerciseService service;

    private ExerciseCategory category() {
        ExerciseCategory c = new ExerciseCategory();
        c.setId(3L);
        c.setName("Pecho");
        return c;
    }

    private ExerciseRequest request() {
        ExerciseRequest r = new ExerciseRequest();
        r.setExerciseCategoryId(3L);
        r.setName("Flexion");
        r.setDescription("d");
        r.setWeighted(true);
        return r;
    }

    @Test
    void getAll_devuelveDtosSinRelaciones() {
        Exercise e = new Exercise();
        e.setId(1L);
        e.setName("Flexion");
        e.setIsWeighted(false);
        e.setExerciseCategory(category());
        when(exerciseRepository.findAll()).thenReturn(List.of(e));

        List<ExerciseResponse> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getExerciseCategoryId());
        assertFalse(result.get(0).isWeighted());
    }

    @Test
    void save_guardaYDevuelveDto() {
        when(exerciseCategoryRepository.findById(3L)).thenReturn(Optional.of(category()));
        when(exerciseRepository.save(any(Exercise.class))).thenAnswer(inv -> {
            Exercise e = inv.getArgument(0);
            e.setId(7L);
            return e;
        });

        ExerciseResponse result = service.save(request());

        assertEquals(7L, result.id);
        assertEquals("Flexion", result.getName());
        assertTrue(result.isWeighted());
    }

    @Test
    void update_devuelveDto() {
        Exercise existing = new Exercise();
        existing.setId(1L);
        existing.setName("Viejo");
        existing.setExerciseCategory(category());
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(exerciseCategoryRepository.findById(3L)).thenReturn(Optional.of(category()));
        when(exerciseRepository.save(existing)).thenReturn(existing);

        ExerciseResponse result = service.update(1L, request());

        assertEquals("Flexion", result.getName());
        assertEquals(3L, result.getExerciseCategoryId());
    }

    @Test
    void update_inexistente_lanzaResourceNotFound() {
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(99L, request()));
    }
}
