package cpe223.group8.eggspress.models;

public class User {
    private String username;
    private String password;
    private String role = "Staff"; // Default fallback role to prevent null scoping exceptions

    // Ensure your constructor takes exactly two strings in this order!
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Overloaded constructor maps fully initialized database records with distinct role scopes
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        if (role != null && !role.isEmpty()) {
            this.role = role;
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        if (role != null && !role.isEmpty()) {
            this.role = role;
        }
    }
}