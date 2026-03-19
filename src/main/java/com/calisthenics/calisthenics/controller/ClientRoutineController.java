package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.ClientRoutineRequest;
import com.calisthenics.calisthenics.dto.ClientRoutineResponse;
import com.calisthenics.calisthenics.model.ClientRoutine;
import com.calisthenics.calisthenics.service.ClientRoutineService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/client-routine")
public class ClientRoutineController {
    private final ClientRoutineService service;
    public ClientRoutineController(ClientRoutineService service){
        this.service = service;
    }

    @PostMapping
    public ClientRoutineResponse create(@RequestBody ClientRoutineRequest clientRoutineRequest) { return this.service.save(clientRoutineRequest); }

    @GetMapping
    public List<ClientRoutine> getAll(){ return this.service.getAll(); }

}
