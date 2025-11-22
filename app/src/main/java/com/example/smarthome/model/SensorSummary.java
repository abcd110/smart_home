package com.example.smarthome.model;

import java.io.Serializable;

public class SensorSummary implements Serializable {
    private Double temperature;
    private Double humidity;
    private Double gas;

    public SensorSummary() {}

    public SensorSummary(Double temperature, Double humidity, Double gas) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.gas = gas;
    }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }
    public Double getGas() { return gas; }
    public void setGas(Double gas) { this.gas = gas; }
}
