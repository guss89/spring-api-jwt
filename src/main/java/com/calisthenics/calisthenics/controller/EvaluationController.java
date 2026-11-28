package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.EvaluationRequest;
import com.calisthenics.calisthenics.dto.EvaluationResponse;
import com.calisthenics.calisthenics.security.CurrentUser;
import com.calisthenics.calisthenics.service.EvaluationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/evaluations")
public class EvaluationController {
    private final EvaluationService service;
    public  EvaluationController(EvaluationService service){ this.service = service; }

    @PostMapping
    public EvaluationResponse create(@Valid @RequestBody EvaluationRequest evaluationRequest){ return this.service.save(evaluationRequest); }

    @GetMapping
    public List<EvaluationResponse> getAll(@RequestParam(required = false) Long userId, Authentication authentication){
        return this.service.getAll(userId, CurrentUser.from(authentication));
    }

    @GetMapping("/{id}")
    public EvaluationResponse getById(@PathVariable Long id, Authentication authentication){
        return this.service.getById(id, CurrentUser.from(authentication));
    }

    @PutMapping("/{id}")
    public EvaluationResponse update(@PathVariable Long id, @Valid @RequestBody EvaluationRequest evaluationRequest){
        return service.update(id, evaluationRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
