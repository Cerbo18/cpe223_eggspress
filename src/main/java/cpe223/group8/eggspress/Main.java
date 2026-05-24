package cpe223.group8.eggspress;

import cpe223.group8.eggspress.config.DatabaseConfig; // Imported your DB configuration class
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.io.InputStream;

public class Main extends Application {
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        stage.initStyle(StageStyle.EXTENDED);

        // 1. Initialize the SQLite database tables before loading the login view
        DatabaseConfig.initializeDatabase();

        scene = new Scene(loadFXML("login"), 760, 520);
        stage.setMinWidth(720);
        stage.setMinHeight(576);
        
        // Icon Loader
        try {
            InputStream stream = Main.class.getResourceAsStream("/kaviyes/nhx/eggspress/1x/Eggspress-App-Icon.png");
            if (stream == null) {
                stream = Main.class.getResourceAsStream("icons/icon.png");
            }
            if (stream == null) {
                stream = Main.class.getResourceAsStream("/cpe223/group8/eggspress/icons/icon.png");
            }
            if (stream != null) {
                stage.getIcons().add(new Image(stream));
                stream.close();
            } else {
                System.out.println("Warning: Window icon path not found. Proceeding with default OS decoration.");
            }
        } catch (Exception e) {
            System.out.println("Warning: Failed to load window icon: " + e.getMessage());
        }

        stage.setTitle("Eggspress Chicken Farm Manager");
        stage.setScene(scene);
        stage.setMaximized(true);
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