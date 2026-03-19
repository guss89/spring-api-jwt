package com.calisthenics.calisthenics.dto;

import java.time.LocalDate;

public class ClientRoutineRequest {
    public Long id;
    public Long userId;
    public Long routineId;
    public LocalDate startDate;
    public LocalDate endDate;
    public String observations;
    public Boolean active;

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
