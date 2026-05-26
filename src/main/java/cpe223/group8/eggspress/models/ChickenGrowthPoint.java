package cpe223.group8.eggspress.models;

public class ChickenGrowthPoint {
    private String recordDate;
    private int flockCount;
    private double averageWeight;

    public ChickenGrowthPoint(String recordDate, int flockCount, double averageWeight) {
        this.recordDate = recordDate;
        this.flockCount = flockCount;
        this.averageWeight = averageWeight;
    }

    public String getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(String recordDate) {
        this.recordDate = recordDate;
    }

    public int getFlockCount() {
        return flockCount;
    }

    public void setFlockCount(int flockCount) {
        this.flockCount = flockCount;
    }

    public double getAverageWeight() {
        return averageWeight;
    }

    public void setAverageWeight(double averageWeight) {
        this.averageWeight = averageWeight;
    }
}
