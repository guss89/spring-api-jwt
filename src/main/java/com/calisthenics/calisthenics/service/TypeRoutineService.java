package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.TypeRoutineRequest;
import com.calisthenics.calisthenics.dto.TypeRoutineResponse;
import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.TypeRoutine;
import com.calisthenics.calisthenics.repository.TypeRoutineRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TypeRoutineService {

    private final TypeRoutineRepository repository;

    public TypeRoutineService(TypeRoutineRepository repository){
        this.repository = repository;
    }

    public TypeRoutineResponse save(TypeRoutineRequest request){
        TypeRoutine typeRoutine = new TypeRoutine();
        typeRoutine.setName(request.getName());
        return TypeRoutineResponse.from(repository.save(typeRoutine));
    }

    public List<TypeRoutineResponse> getAll(){
        return repository.findAll().stream().map(TypeRoutineResponse::from).toList();
    }

    public TypeRoutine findById(Long id){
        return repository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Type Routine not found"));
    }

    public TypeRoutineResponse update(Long id, TypeRoutineRequest request){
        TypeRoutine existing = findById(id);
        existing.setName(request.getName());

        return TypeRoutineResponse.from(repository.save(existing));
    }

    public void delete(Long id){
        TypeRoutine existing = findById(id);
        repository.delete(existing);
    }
}
