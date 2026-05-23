package cpe223.group8.eggspress;

import cpe223.group8.eggspress.config.DatabaseConfig; // Imported your DB configuration class
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class Main extends Application {
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // 1. Initialize the SQLite database tables before loading the login view
        DatabaseConfig.initializeDatabase();

        scene = new Scene(loadFXML("login"), 640, 480);
        
        // 2. Modified icon loader with a null check to prevent app crashes if icon path is misconfigured
        InputStream iconStream = getClass().getResourceAsStream("/cpe223.group8.eggspress/icons/icon.png");
        if (iconStream != null) {
            Image icon = new Image(iconStream);
            stage.getIcons().add(icon);
        } else {
            System.out.println("Warning: Window icon path not found. Proceeding with default OS decoration.");
        }

        stage.setTitle("Chicken Eggspress");
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("views/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}