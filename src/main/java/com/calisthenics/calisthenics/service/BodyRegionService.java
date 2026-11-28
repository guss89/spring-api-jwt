package com.calisthenics.calisthenics.service;

import com.calisthenics.calisthenics.dto.BodyRegionRequest;
import com.calisthenics.calisthenics.dto.BodyRegionResponse;
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

    public BodyRegionResponse save(BodyRegionRequest request){
        BodyRegion bodyRegion = new BodyRegion();
        bodyRegion.setName(request.getName());
        return BodyRegionResponse.from(this.repository.save(bodyRegion));
    }

    public List<BodyRegionResponse> getAll(){
        return repository.findAll().stream().map(BodyRegionResponse::from).toList();
    }

    public BodyRegion findById(Long id){
        return repository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("BodyRegion not found"));
    }

    public BodyRegionResponse update(Long id, BodyRegionRequest request){
        BodyRegion existing = findById(id);
        existing.setName(request.getName());
        return BodyRegionResponse.from(repository.save(existing));
    }

    public void delete(Long id){
        BodyRegion existing = findById(id);
        repository.delete(existing);
    }

}
