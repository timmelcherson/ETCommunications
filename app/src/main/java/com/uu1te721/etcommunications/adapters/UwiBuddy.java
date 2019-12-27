package com.uu1te721.etcommunications.adapters;

public class UwiBuddy {

    private Integer distance;
    private Integer budyID;
    private String alias;
    private Integer Xposition;
    private Integer Yposition;


    public UwiBuddy(Integer EUI){
        this.budyID = EUI;
    }

    public void setAlias(String alias){
        this.alias = alias;
    }

    public Integer getDistance(){
        return this.distance;
    }


    public void setPosition(Integer x, Integer y){
        this.Xposition = x;
        this.Yposition = y;
    }

    public int getXposition(){
        return Xposition;
    }

    public int getYposition(){
        return Yposition;
    }
}
