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
class ExerciseCategoryServiceTest {

    @Mock private ExerciseCategoryRepository repository;
    @InjectMocks private ExerciseCategoryService service;

    private ExerciseCategory entity(Long id, String name) {
        ExerciseCategory e = new ExerciseCategory();
        e.setId(id);
        e.setName(name);
        return e;
    }

    @Test
    void getAll_devuelveDtos() {
        when(repository.findAll()).thenReturn(List.of(entity(1L, "Pecho")));

        List<ExerciseCategoryResponse> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Pecho", result.get(0).getName());
    }

    @Test
    void save_guardaYDevuelveDto() {
        when(repository.save(any(ExerciseCategory.class))).thenAnswer(inv -> {
            ExerciseCategory e = inv.getArgument(0);
            e.setId(7L);
            return e;
        });

        ExerciseCategoryResponse result = service.save(new ExerciseCategoryRequest("Pecho"));

        assertEquals(7L, result.getId());
        assertEquals("Pecho", result.getName());
    }

    @Test
    void update_cambiaNombre() {
        ExerciseCategory existing = entity(1L, "Viejo");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        ExerciseCategoryResponse result = service.update(1L, new ExerciseCategoryRequest("Nuevo"));

        assertEquals("Nuevo", result.getName());
    }

    @Test
    void update_inexistente_lanzaResourceNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(99L, new ExerciseCategoryRequest("X")));
        verify(repository, never()).save(any());
    }
}
