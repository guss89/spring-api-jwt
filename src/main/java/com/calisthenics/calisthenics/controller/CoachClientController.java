package com.calisthenics.calisthenics.controller;

import com.calisthenics.calisthenics.dto.CoachClientRequest;
import com.calisthenics.calisthenics.dto.CoachClientResponse;
import com.calisthenics.calisthenics.security.CurrentUser;
import com.calisthenics.calisthenics.service.CoachClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coach-clients")
public class CoachClientController {
    private final CoachClientService service;

    public CoachClientController(CoachClientService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CoachClientResponse> create(@Valid @RequestBody CoachClientRequest request,
                                                      Authentication authentication) {
        CoachClientResponse created = service.assignCoachToClient(request, CurrentUser.from(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<CoachClientResponse> getAll(@RequestParam(required = false) Long coachId,
                                            @RequestParam(required = false) Long clientId,
                                            Authentication authentication) {
        if ((coachId == null) == (clientId == null)) {
            throw new IllegalArgumentException("Indique exactamente uno de los parámetros coachId o clientId");
        }
        CurrentUser caller = CurrentUser.from(authentication);
        return coachId != null
                ? service.getClientsByCoach(coachId, caller)
                : service.getCoachesByClient(clientId, caller);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, CurrentUser.from(authentication));
        return ResponseEntity.noContent().build();
    }
}
