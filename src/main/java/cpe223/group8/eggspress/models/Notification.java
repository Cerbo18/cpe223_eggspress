package cpe223.group8.eggspress.models;

public class Notification {
    private int id;
    private String timestamp;
    private String level;
    private String message;
    private boolean isRead;

    // Full constructor
    public Notification(int id, String timestamp, String level, String message, boolean isRead) {
        this.id = id;
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
        this.isRead = isRead;
    }

    // Helper constructor for creation
    public Notification(String level, String message) {
        this.id = 0;
        this.timestamp = "";
        this.level = level;
        this.message = message;
        this.isRead = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
