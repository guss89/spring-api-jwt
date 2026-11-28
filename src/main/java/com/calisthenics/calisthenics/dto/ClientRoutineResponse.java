package com.calisthenics.calisthenics.dto;

import com.calisthenics.calisthenics.model.ClientRoutine;

import java.time.LocalDate;

public class ClientRoutineResponse {
    public Long id;
    public Long userId;
    public Long routineId;
    public LocalDate startDate;
    public LocalDate endDate;
    public String observations;
    public Boolean active;

    public ClientRoutineResponse(
            Long id,
            Long userId,
            Long routineId,
            LocalDate startDate,
            LocalDate endDate,
            String observations,
            Boolean active
    ){
        this.id = id;
        this.userId = userId;
        this.routineId = routineId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.observations = observations;
        this.active = active;
    }

    public static ClientRoutineResponse from(ClientRoutine cr) {
        return new ClientRoutineResponse(
                cr.getId(),
                cr.getUser().getId(),
                cr.getRoutine().getId(),
                cr.getStartDate(),
                cr.getEndDate(),
                cr.getObservations(),
                cr.getActive()
        );
    }

    public void setId(Long id){ this.id = id;}
    public void setUserId(Long userId){ this.userId = userId;}
    public void setRoutineId(Long routineId){ this.routineId = routineId;}
    public void setStartDate(LocalDate startDate){ this.startDate = startDate;}
    public void setEndDate(LocalDate endDate){ this.endDate = endDate;}
    public void setObservations(String observations){ this.observations = observations; }
    public void setActive(Boolean active){ this.active = active; }

    public Long getId(){ return id; }
    public Long getUserId(){ return userId; }
    public Long getRoutineId(){ return routineId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getObservations() { return observations;}
    public Boolean getActive() { return active;}
}
