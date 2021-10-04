package org.djna.asynch.estate.data;

import java.util.Date;

public class ThermostatReading {
    private Date date;
    private int temperature;
    private String location;

    public ThermostatReading(int initTemperature, String initLocation){
        date = new Date();
        temperature = initTemperature;
        location = initLocation;
    }

    // Jackson parser requries a zero-arg constructor
    // values are later set correctly when JSON is parsed
    public ThermostatReading(){
        date = new Date();
        temperature = 0;
        location = "unknown";
    }

    public Date getDate() {
        return date;
    }

    public int getTemperature() {
        return temperature;
    }

    public String getLocation() {
        return location;
    }



    @Override
    public String toString() {
        return "ThermostatReading{" +
                "date=" + date +
                ", temperature=" + temperature +
                ", location='" + location + '\'' +
                '}';
    }
}
