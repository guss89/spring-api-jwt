package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.exception.ResourceNotFoundException;
import com.calisthenics.calisthenics.model.BodyRegion;
import com.calisthenics.calisthenics.repository.BodyRegionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BodyRegionService {

    private final BodyRegionRepository repository;

    public BodyRegionService(BodyRegionRepository repository){
        this.repository = repository;
    }

    public BodyRegion save( BodyRegion bodyRegion){
        return this.repository.save(bodyRegion);
    }

    public List<BodyRegion> getAll(){
        return repository.findAll();
    }

    public BodyRegion findById(Long id){
        return repository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("BodyRegion not found"));
    }

    public BodyRegion update( Long id, BodyRegion bodyRegion){
        BodyRegion existing = findById(id);
        existing.setName(bodyRegion.getName());
        return  repository.save(existing);
    }

    public void delete(Long id){
        BodyRegion existing = findById(id);
        repository.delete(existing);
    }

}
