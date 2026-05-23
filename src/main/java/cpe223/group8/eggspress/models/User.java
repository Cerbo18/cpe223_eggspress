package cpe223.group8.eggspress.models;

public class User {
    private String username;
    private String password;

    // Ensure your constructor takes exactly two strings in this order!
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}