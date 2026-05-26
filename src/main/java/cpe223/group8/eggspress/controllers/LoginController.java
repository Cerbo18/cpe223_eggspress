package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.Main;
import cpe223.group8.eggspress.models.User;
import cpe223.group8.eggspress.repository.UserRepository;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.SVGPath;
import javafx.application.Platform;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private VBox usernameStep;

    @FXML
    private VBox passwordStep;

    @FXML
    private Label userConfirmationLabel;

    @FXML
    private VBox usernameActions;

    @FXML
    private HBox passwordActions;

    @FXML
    private HBox errorContainer;

    @FXML
    private HeaderBar headerBar;

    @FXML
    private Button themeToggleBtn;

    @FXML
    private SVGPath themeToggleIcon;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        // Automatically clear error states when typing
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            usernameField.getStyleClass().remove("text-input-error");
            errorContainer.setVisible(false);
            errorContainer.setManaged(false);
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            passwordField.getStyleClass().remove("text-input-error");
            errorContainer.setVisible(false);
            errorContainer.setManaged(false);
        });

        // Attach window drag listeners at the scene level after layout to avoid
        // conflicting with HeaderBar's internal gesture state machine
        if (headerBar != null) {
            headerBar.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                        if (event.getY() <= headerBar.getHeight()) {
                            Stage stage = (Stage) newScene.getWindow();
                            if (!Double.isNaN(stage.getX())) {
                                xOffset = event.getScreenX() - stage.getX();
                                yOffset = event.getScreenY() - stage.getY();
                            }
                        }
                    });

                    newScene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_DRAGGED, event -> {
                        if (event.getY() <= headerBar.getHeight()) {
                            Stage stage = (Stage) newScene.getWindow();
                            if (!stage.isMaximized() && xOffset != 0 && yOffset != 0) {
                                stage.setX(event.getScreenX() - xOffset);
                                stage.setY(event.getScreenY() - yOffset);
                            }
                        }
                    });

                    newScene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_RELEASED, event -> {
                        xOffset = 0;
                        yOffset = 0;
                    });
                }
            });
        }

        Platform.runLater(() -> {
            if (themeToggleIcon != null) {
                if (cpe223.group8.eggspress.services.ThemeManager.isDarkMode()) {
                    themeToggleIcon.setContent("M8 12a4 4 0 1 0 8 0a4 4 0 1 0 -8 0 M3 12h1m8 -9v1m8 8h1m-9 8v1m-6.4 -15.4l.7 .7m12.1 -.7l-.7 .7m0 11.4l.7 .7m-12.1 -.7l-.7 .7");
                } else {
                    themeToggleIcon.setContent("M12 3c.132 0 .263 0 .393 0a7.5 7.5 0 0 0 7.92 12.446a9 9 0 1 1 -8.313 -12.454l0 .008");
                }
            }
        });
    }

    @FXML
    private void handleNext() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            usernameField.getStyleClass().remove("text-input-error");
            usernameField.getStyleClass().add("text-input-error");
            messageLabel.setText("Please enter your username.");
            errorContainer.setVisible(true);
            errorContainer.setManaged(true);
            return;
        }

        // Clear error states
        usernameField.getStyleClass().remove("text-input-error");
        errorContainer.setVisible(false);
        errorContainer.setManaged(false);

        // fade-out transition for username inputs and action buttons together
        FadeTransition fadeOutStep = new FadeTransition(Duration.millis(150), usernameStep);
        fadeOutStep.setFromValue(1.0);
        fadeOutStep.setToValue(0.0);

        FadeTransition fadeOutActions = new FadeTransition(Duration.millis(150), usernameActions);
        fadeOutActions.setFromValue(1.0);
        fadeOutActions.setToValue(0.0);

        fadeOutStep.setOnFinished(event -> {
            usernameStep.setVisible(false);
            usernameStep.setManaged(false);
            usernameActions.setVisible(false);
            usernameActions.setManaged(false);

            userConfirmationLabel.setText(username);

            passwordStep.setVisible(true);
            passwordStep.setManaged(true);
            passwordStep.setOpacity(0.0);

            passwordActions.setVisible(true);
            passwordActions.setManaged(true);
            passwordActions.setOpacity(0.0);

            // Smooth fade-in transition for password step and actions
            FadeTransition fadeInStep = new FadeTransition(Duration.millis(150), passwordStep);
            fadeInStep.setFromValue(0.0);
            fadeInStep.setToValue(1.0);
            fadeInStep.play();

            FadeTransition fadeInActions = new FadeTransition(Duration.millis(150), passwordActions);
            fadeInActions.setFromValue(0.0);
            fadeInActions.setToValue(1.0);
            fadeInActions.play();

            passwordField.requestFocus();
        });

        fadeOutStep.play();
        fadeOutActions.play();
    }

    @FXML
    private void handleBack() {
        // Clear error states
        passwordField.getStyleClass().remove("text-input-error");
        errorContainer.setVisible(false);
        errorContainer.setManaged(false);

        // fade-out transition for password inputs and action buttons
        FadeTransition fadeOutStep = new FadeTransition(Duration.millis(150), passwordStep);
        fadeOutStep.setFromValue(1.0);
        fadeOutStep.setToValue(0.0);

        FadeTransition fadeOutActions = new FadeTransition(Duration.millis(150), passwordActions);
        fadeOutActions.setFromValue(1.0);
        fadeOutActions.setToValue(0.0);

        fadeOutStep.setOnFinished(event -> {
            passwordStep.setVisible(false);
            passwordStep.setManaged(false);
            passwordActions.setVisible(false);
            passwordActions.setManaged(false);

            usernameStep.setVisible(true);
            usernameStep.setManaged(true);
            usernameStep.setOpacity(0.0);

            usernameActions.setVisible(true);
            usernameActions.setManaged(true);
            usernameActions.setOpacity(0.0);

            // Smooth fade-in transition for username step and actions
            FadeTransition fadeInStep = new FadeTransition(Duration.millis(150), usernameStep);
            fadeInStep.setFromValue(0.0);
            fadeInStep.setToValue(1.0);
            fadeInStep.play();

            FadeTransition fadeInActions = new FadeTransition(Duration.millis(150), usernameActions);
            fadeInActions.setFromValue(0.0);
            fadeInActions.setToValue(1.0);
            fadeInActions.play();

            usernameField.requestFocus();
        });

        fadeOutStep.play();
        fadeOutActions.play();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (password.isEmpty()) {
            passwordField.getStyleClass().remove("text-input-error");
            passwordField.getStyleClass().add("text-input-error");
            messageLabel.setText("Please enter your password.");
            errorContainer.setVisible(true);
            errorContainer.setManaged(true);
            return;
        }

        boolean isAuthenticated = false;
        User authenticatedUser = null;
        for (User user : UserRepository.getStaticUsers()) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                isAuthenticated = true;
                authenticatedUser = user;
                break;
            }
        }

        if (isAuthenticated) {
            try {
                cpe223.group8.eggspress.services.SessionManager.login(authenticatedUser);
                Main.setRoot("dashboard");
            } catch (IOException e) {
                passwordField.getStyleClass().remove("text-input-error");
                passwordField.getStyleClass().add("text-input-error");
                messageLabel.setText("Error loading dashboard view.");
                errorContainer.setVisible(true);
                errorContainer.setManaged(true);
                e.printStackTrace();
            }
        } else {
            passwordField.getStyleClass().remove("text-input-error");
            passwordField.getStyleClass().add("text-input-error");
            messageLabel.setText("Invalid credentials. Try again.");
            errorContainer.setVisible(true);
            errorContainer.setManaged(true);
        }
    }

    @FXML
    private void handleToggleTheme() {
        boolean dark = !cpe223.group8.eggspress.services.ThemeManager.isDarkMode();
        cpe223.group8.eggspress.services.ThemeManager.setDarkMode(dark);

        // Apply theme to the current login root scene
        if (themeToggleBtn != null && themeToggleBtn.getScene() != null) {
            Parent root = themeToggleBtn.getScene().getRoot();
            cpe223.group8.eggspress.services.ThemeManager.applyTheme(root);
        }

        // Update toggle button icon path (Moon for Light Mode, Sun for Dark Mode)
        if (themeToggleIcon != null) {
            if (dark) {
                themeToggleIcon.setContent("M8 12a4 4 0 1 0 8 0a4 4 0 1 0 -8 0 M3 12h1m8 -9v1m8 8h1m-9 8v1m-6.4 -15.4l.7 .7m12.1 -.7l-.7 .7m0 11.4l.7 .7m-12.1 -.7l-.7 .7");
            } else {
                themeToggleIcon.setContent("M12 3c.132 0 .263 0 .393 0a7.5 7.5 0 0 0 7.92 12.446a9 9 0 1 1 -8.313 -12.454l0 .008");
            }
        }
    }
}
