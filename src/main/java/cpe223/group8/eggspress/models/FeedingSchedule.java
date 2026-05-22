package cpe223.group8.eggspress.models;

public class FeedingSchedule {
    private String category;
    private String time;
    private String feedingType;
    private String status;

    public FeedingSchedule(String category, String time, String feedingType, String status) {
        this.category = category;
        this.time = time;
        this.feedingType = feedingType;
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFeedingType() {
        return feedingType;
    }

    public void setFeedingType(String feedingType) {
        this.feedingType = feedingType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
