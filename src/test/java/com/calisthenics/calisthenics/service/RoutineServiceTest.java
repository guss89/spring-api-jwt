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
class RoutineServiceTest {

    @Mock private TypeRoutineRepository typeRoutineRepository;
    @Mock private RoutineRepository routineRepository;
    @InjectMocks private RoutineService service;

    private TypeRoutine type() {
        TypeRoutine t = new TypeRoutine();
        t.setId(3L);
        t.setName("Fuerza");
        return t;
    }

    private RoutineRequest request() {
        RoutineRequest r = new RoutineRequest();
        r.setTypeRoutineId(3L);
        r.setName("Rutina A");
        return r;
    }

    @Test
    void getAll_devuelveDtos() {
        Routine r = new Routine();
        r.setId(1L);
        r.setName("Rutina A");
        r.setTypeRoutine(type());
        when(routineRepository.findAll()).thenReturn(List.of(r));

        List<RoutineResponse> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getTypeRoutineId());
    }

    @Test
    void save_guardaYDevuelveDto() {
        when(typeRoutineRepository.findById(3L)).thenReturn(Optional.of(type()));
        when(routineRepository.save(any(Routine.class))).thenAnswer(inv -> {
            Routine r = inv.getArgument(0);
            r.setId(7L);
            return r;
        });

        RoutineResponse result = service.save(request());

        assertEquals(7L, result.getId());
        assertEquals("Rutina A", result.getName());
    }

    @Test
    void update_devuelveDto() {
        Routine existing = new Routine();
        existing.setId(1L);
        existing.setName("Vieja");
        existing.setTypeRoutine(type());
        when(routineRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(typeRoutineRepository.findById(3L)).thenReturn(Optional.of(type()));
        when(routineRepository.save(existing)).thenReturn(existing);

        RoutineResponse result = service.update(1L, request());

        assertEquals("Rutina A", result.getName());
    }

    @Test
    void update_inexistente_lanzaResourceNotFound() {
        when(routineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(99L, request()));
    }
}
