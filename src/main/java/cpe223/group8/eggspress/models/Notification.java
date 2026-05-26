package cpe223.group8.eggspress.models;

public class Notification {
    private int id;
    private String timestamp;
    private String level;
    private String message;
    private boolean isRead;
    private String username; // Nullable for global notifications
    private int priority = 1; // 1 = Normal, 2 = Low

    // Full constructor with username and priority
    public Notification(int id, String timestamp, String level, String message, boolean isRead, String username, int priority) {
        this.id = id;
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
        this.priority = priority;
        this.isRead = (priority == 2) || isRead;
        this.username = username;
    }

    // Full constructor with username
    public Notification(int id, String timestamp, String level, String message, boolean isRead, String username) {
        this(id, timestamp, level, message, isRead, username, 1);
    }

    // Backward-compatible full constructor
    public Notification(int id, String timestamp, String level, String message, boolean isRead) {
        this(id, timestamp, level, message, isRead, null, 1);
    }

    // Helper constructor for creation with username and priority
    public Notification(String level, String message, String username, int priority) {
        this.id = 0;
        this.timestamp = "";
        this.level = level;
        this.message = message;
        this.priority = priority;
        this.isRead = (priority == 2);
        this.username = username;
    }

    // Helper constructor for creation with username
    public Notification(String level, String message, String username) {
        this(level, message, username, 1);
    }

    // Backward-compatible helper constructor for creation
    public Notification(String level, String message) {
        this(level, message, null, 1);
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        if (priority == 2) {
            this.isRead = true;
        }
    }
}
