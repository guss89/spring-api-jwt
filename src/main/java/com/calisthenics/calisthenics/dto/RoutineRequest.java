package com.calisthenics.calisthenics.dto;


public class RoutineRequest {

    private Long typeRoutineId;
    private String name;

    public Long getTypeRoutineId(){
        return  typeRoutineId;
    }
    public String getName(){ return name; }

    public void setName(String name){
        this.name = name;
    }

    public void setTypeRoutineId(Long id){
        this.typeRoutineId = id;
    }
}
