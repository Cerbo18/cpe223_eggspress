package cpe223.group8.eggspress.services;

import javafx.collections.ObservableList;
import javafx.scene.Parent;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the application's active theme (Light/Dark) and
 * dynamically updates JavaFX Node stylesheets.
 */
public class ThemeManager {
    private static boolean darkMode = false;

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean dark) {
        darkMode = dark;
    }

    /**
     * Dynamically swaps any theme-specific stylesheets on the given Parent.
     * Replaces '/light.css' with '/dark.css' and vice versa.
     */
    public static void applyTheme(Parent root) {
        if (root == null) {
            return;
        }

        ObservableList<String> stylesheets = root.getStylesheets();
        List<String> updated = new ArrayList<>(stylesheets);
        boolean changed = false;

        for (int i = 0; i < updated.size(); i++) {
            String url = updated.get(i);
            String newUrl = swapThemeInUrl(url, darkMode);
            if (!url.equals(newUrl)) {
                updated.set(i, newUrl);
                changed = true;
            }
        }

        if (changed) {
            stylesheets.setAll(updated);
        }

        if (root.getScene() != null) {
            applySceneFill(root.getScene());
        }
    }

    public static void applySceneFill(javafx.scene.Scene scene) {
        if (scene == null) {
            return;
        }
        if (darkMode) {
            scene.setFill(javafx.scene.paint.Color.web("#1d1d1f"));
        } else {
            scene.setFill(javafx.scene.paint.Color.web("#f5f5f7"));
        }
    }

    private static String swapThemeInUrl(String url, boolean dark) {
        if (dark) {
            // Swap light -> dark
            if (url.endsWith("/light.css")) {
                return url.substring(0, url.length() - 9) + "dark.css";
            }
            if (url.contains("/light.css")) {
                return url.replace("/light.css", "/dark.css");
            }
            if (url.contains("light.css")) {
                return url.replace("light.css", "dark.css");
            }
        } else {
            // Swap dark -> light
            if (url.endsWith("/dark.css")) {
                return url.substring(0, url.length() - 8) + "light.css";
            }
            if (url.contains("/dark.css")) {
                return url.replace("/dark.css", "/light.css");
            }
            if (url.contains("dark.css")) {
                return url.replace("dark.css", "light.css");
            }
        }
        return url;
    }
}
