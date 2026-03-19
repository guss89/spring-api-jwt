package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.RoutineRequest;
import com.calisthenics.calisthenics.dto.RoutineResponse;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.Routine;
import com.calisthenics.calisthenics.model.TypeRoutine;
import com.calisthenics.calisthenics.repository.RoutineRepository;
import com.calisthenics.calisthenics.repository.TypeRoutineRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoutineService {
    private final TypeRoutineRepository typeRoutineRepository;
    private final RoutineRepository routineRepository;

    public RoutineService(TypeRoutineRepository typeRoutineRepository, RoutineRepository routineRepository){
        this.typeRoutineRepository = typeRoutineRepository;
        this.routineRepository = routineRepository;
    }

    public RoutineResponse save(RoutineRequest request){
        TypeRoutine typeRoutine = typeRoutineRepository.findById(request.getTypeRoutineId())
                .orElseThrow(()->new ResourceNotFoundException("Type Routine not found"));

        Routine routine =  new Routine();
        routine.setTypeRoutine(typeRoutine);
        routine.setName(request.getName());

        Routine saved = routineRepository.save(routine);

        return new RoutineResponse(
            saved.getId(),
            saved.getName(),
            saved.getTypeRoutine().getId()
        );
    }

    public List<Routine> getAll(){ return routineRepository.findAll(); }

    public  Routine findById(Long id){
        return  routineRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Routine not found"));
    }

    public Routine update(Long id, RoutineRequest routineRequest){
        Routine routine = routineRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Routine not found"));
         TypeRoutine typeRoutine = typeRoutineRepository.findById(routineRequest.getTypeRoutineId())
                 .orElseThrow(()->new ResourceNotFoundException("Routine not found"));

         routine.setName(routineRequest.getName());
         routine.setTypeRoutine(typeRoutine);

        return routineRepository.save(routine);
    }

    public void delete(Long id){
        Routine existing = findById(id);
        routineRepository.delete(existing);
    }
}
