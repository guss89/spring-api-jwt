package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.MembershipPlanRequest;
import com.calisthenics.calisthenics.dto.MembershipPlanResponse;
import com.calisthenics.calisthenics.service.MembershipPlanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/membership-plans")
public class MembershipPlanController {
    private final MembershipPlanService service;
    public MembershipPlanController(MembershipPlanService service){ this.service = service; }

    @PostMapping
    public MembershipPlanResponse create(@Valid @RequestBody MembershipPlanRequest request){
        return this.service.save(request);
    }

    @GetMapping
    public List<MembershipPlanResponse> getAll(){ return this.service.getAll(); }

    @GetMapping("/{id}")
    public MembershipPlanResponse getById(@PathVariable Long id){ return this.service.getById(id); }

    @PutMapping("/{id}")
    public MembershipPlanResponse update(@PathVariable Long id, @Valid @RequestBody MembershipPlanRequest request){
        return this.service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
