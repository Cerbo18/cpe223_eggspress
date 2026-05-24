package cpe223.group8.eggspress.models;

public class ChickenHouse {
    private String id;
    private String name;
    private int flockCount;
    private String status;

    public ChickenHouse(String id, String name, int flockCount, String status) {
        this.id = id;
        this.name = name;
        this.flockCount = flockCount;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFlockCount() {
        return flockCount;
    }

    public void setFlockCount(int flockCount) {
        this.flockCount = flockCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

