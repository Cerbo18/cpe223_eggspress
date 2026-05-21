package com.eggspress.models;

public class Automation {
    private String id;
    private String waterSource;
    private String location;
    private double amount;
    private String status;

    public Automation(String id, String waterSource, String location, double amount, String status) {
        this.id = id;
        this.waterSource = waterSource;
        this.location = location;
        this.amount = amount;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWaterSource() {
        return waterSource;
    }

    public void setWaterSource(String waterSource) {
        this.waterSource = waterSource;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
