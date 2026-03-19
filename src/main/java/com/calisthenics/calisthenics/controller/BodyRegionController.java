package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.model.BodyRegion;
import com.calisthenics.calisthenics.service.BodyRegionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/body-regions")
public class BodyRegionController {
    private final BodyRegionService service;

    public BodyRegionController(BodyRegionService service){
        this.service = service;
    }

    @PostMapping
    public BodyRegion create(@RequestBody BodyRegion bodyRegion){
        return  service.save(bodyRegion);
    }

    @GetMapping
    public List<BodyRegion> getAll(){
        return service.getAll();
    }

    @PutMapping("/{id}")
    public BodyRegion update(@PathVariable Long id, @RequestBody BodyRegion bodyRegion){
        return service.update(id, bodyRegion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete (@PathVariable Long id){
        service.delete(id);
        return  ResponseEntity.noContent().build();
    }
}
