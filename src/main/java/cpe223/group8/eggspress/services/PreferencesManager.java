package cpe223.group8.eggspress.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Service to manage loading, caching, and persisting application settings.
 * Saves configurations to a persistent file in the user's home directory.
 */
public class PreferencesManager {

    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.eggspress";
    private static final String CONFIG_FILE = CONFIG_DIR + "/settings.properties";

    private static Properties properties = new Properties();

    static {
        loadSettings();
    }

    public static void loadSettings() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            // Apply default configurations if no settings file is detected
            properties.setProperty("startingTheme", "LIGHT");
            properties.setProperty("enablePushToasts", "true");
            properties.setProperty("toastPriorityFilter", "INFO");
            properties.setProperty("developerMode", "false");
            saveSettings();
            return;
        }

        try (FileInputStream in = new FileInputStream(file)) {
            properties.load(in);
        } catch (IOException e) {
            System.err.println("Failed to read user configurations: " + e.getMessage());
        }
    }

    public static void saveSettings() {
        File dir = new File(CONFIG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            properties.store(out, "Eggspress Application User Preferences");
        } catch (IOException e) {
            System.err.println("Failed to persist user configurations: " + e.getMessage());
        }
    }

    public static String getStartingTheme() {
        return properties.getProperty("startingTheme", "LIGHT");
    }

    public static void setStartingTheme(String theme) {
        properties.setProperty("startingTheme", theme);
        saveSettings();
    }

    public static boolean isPushToastsEnabled() {
        return Boolean.parseBoolean(properties.getProperty("enablePushToasts", "true"));
    }

    public static void setPushToastsEnabled(boolean enabled) {
        properties.setProperty("enablePushToasts", String.valueOf(enabled));
        saveSettings();
    }

    public static String getToastPriorityFilter() {
        return properties.getProperty("toastPriorityFilter", "INFO").toUpperCase();
    }

    public static void setToastPriorityFilter(String level) {
        properties.setProperty("toastPriorityFilter", level.toUpperCase());
        saveSettings();
    }

    public static boolean isDeveloperMode() {
        return Boolean.parseBoolean(properties.getProperty("developerMode", "false"));
    }

    public static void setDeveloperMode(boolean enabled) {
        properties.setProperty("developerMode", String.valueOf(enabled));
        saveSettings();
    }
}
