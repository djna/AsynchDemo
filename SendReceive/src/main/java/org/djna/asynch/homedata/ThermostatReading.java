package org.djna.asynch.homedata;

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
