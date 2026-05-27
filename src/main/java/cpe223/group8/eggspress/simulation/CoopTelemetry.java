package cpe223.group8.eggspress.simulation;

public class CoopTelemetry {
    private String coopId;
    private String coopName;
    private double temperature;
    private double humidity;
    private String fanSpeed;      // "STANDBY", "ACTIVE (Low)", "ACTIVE (Med)", "MAX SPEED"
    private String misterStatus;  // "STANDBY", "ACTIVE"
    private String feederStatus;  // "STANDBY", "ACTIVE"

    public CoopTelemetry(String coopId, String coopName, double temperature, double humidity, String fanSpeed, String misterStatus, String feederStatus) {
        this.coopId = coopId;
        this.coopName = coopName;
        this.temperature = temperature;
        this.humidity = humidity;
        this.fanSpeed = fanSpeed;
        this.misterStatus = misterStatus;
        this.feederStatus = feederStatus;
    }

    public String getCoopId() {
        return coopId;
    }

    public void setCoopId(String coopId) {
        this.coopId = coopId;
    }

    public String getCoopName() {
        return coopName;
    }

    public void setCoopName(String coopName) {
        this.coopName = coopName;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public String getFanSpeed() {
        return fanSpeed;
    }

    public void setFanSpeed(String fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public String getMisterStatus() {
        return misterStatus;
    }

    public void setMisterStatus(String misterStatus) {
        this.misterStatus = misterStatus;
    }

    public String getFeederStatus() {
        return feederStatus;
    }

    public void setFeederStatus(String feederStatus) {
        this.feederStatus = feederStatus;
    }
}
