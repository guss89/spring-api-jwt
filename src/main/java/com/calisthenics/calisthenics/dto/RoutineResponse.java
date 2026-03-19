package com.calisthenics.calisthenics.dto;

public class RoutineResponse {

    Long id;
    String name;
    Long typeRoutineId;

    public RoutineResponse(Long id, String name, Long typeRoutineId) {
        this.id = id;
        this.name = name;
        this.typeRoutineId = typeRoutineId;
    }

    public Long getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }

    public Long getTypeRoutineId(){
        return this.typeRoutineId;
    }

    public void setId(Long id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setTypeRoutineId(Long id){
        this.typeRoutineId = id;
    }
}
