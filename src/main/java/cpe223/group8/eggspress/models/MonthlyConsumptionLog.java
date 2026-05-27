package cpe223.group8.eggspress.models;

/**
 * Domain model representing a single logged monthly feed and water consumption entry.
 * Tracks actual vs estimated usage based on active flock sizes.
 */
public class MonthlyConsumptionLog {
    private int id;
    private String monthYear;
    private int flockCount;
    private double estimatedFeed;
    private double estimatedWater;
    private double actualFeed;
    private double actualWater;
    private String loggedAt;

    // Constructor for mapping retrieved physical database rows
    public MonthlyConsumptionLog(int id, String monthYear, int flockCount, double estimatedFeed, 
                                 double estimatedWater, double actualFeed, double actualWater, String loggedAt) {
        this.id = id;
        this.monthYear = monthYear;
        this.flockCount = flockCount;
        this.estimatedFeed = estimatedFeed;
        this.estimatedWater = estimatedWater;
        this.actualFeed = actualFeed;
        this.actualWater = actualWater;
        this.loggedAt = loggedAt;
    }

    // Constructor for creating new in-memory records prior to SQLite insertion
    public MonthlyConsumptionLog(String monthYear, int flockCount, double estimatedFeed, 
                                 double estimatedWater, double actualFeed, double actualWater) {
        this.monthYear = monthYear;
        this.flockCount = flockCount;
        this.estimatedFeed = estimatedFeed;
        this.estimatedWater = estimatedWater;
        this.actualFeed = actualFeed;
        this.actualWater = actualWater;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public int getFlockCount() {
        return flockCount;
    }

    public void setFlockCount(int flockCount) {
        this.flockCount = flockCount;
    }

    public double getEstimatedFeed() {
        return estimatedFeed;
    }

    public void setEstimatedFeed(double estimatedFeed) {
        this.estimatedFeed = estimatedFeed;
    }

    public double getEstimatedWater() {
        return estimatedWater;
    }

    public void setEstimatedWater(double estimatedWater) {
        this.estimatedWater = estimatedWater;
    }

    public double getActualFeed() {
        return actualFeed;
    }

    public void setActualFeed(double actualFeed) {
        this.actualFeed = actualFeed;
    }

    public double getActualWater() {
        return actualWater;
    }

    public void setActualWater(double actualWater) {
        this.actualWater = actualWater;
    }

    public String getLoggedAt() {
        return loggedAt;
    }

    public void setLoggedAt(String loggedAt) {
        this.loggedAt = loggedAt;
    }
}
