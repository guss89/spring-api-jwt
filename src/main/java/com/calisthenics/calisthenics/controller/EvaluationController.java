package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.EvaluationRequest;
import com.calisthenics.calisthenics.dto.EvaluationResponse;
import com.calisthenics.calisthenics.model.Evaluation;
import com.calisthenics.calisthenics.service.EvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/evaluations")
public class EvaluationController {
    private final EvaluationService service;
    public  EvaluationController(EvaluationService service){ this.service = service; }

    @PostMapping
    public EvaluationResponse create(@RequestBody EvaluationRequest evaluationRequest){ return this.service.save(evaluationRequest); }

    @GetMapping
    public List<Evaluation> getAll(){ return this.service.getAll(); }

    @PutMapping({"/{id}"})
    public Evaluation update(@PathVariable Long id, @RequestBody EvaluationRequest evaluationRequest){
        return service.update(id, evaluationRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
