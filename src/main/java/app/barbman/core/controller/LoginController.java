package app.barbman.core.controller;

import app.barbman.core.model.User;
import app.barbman.core.repositories.users.UsersRepository;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import app.barbman.core.util.PhraseLoaderUtil;
import app.barbman.core.util.SessionManager;
import app.barbman.core.util.WindowManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalTime;
import java.util.ResourceBundle;

/**
 * Controller for the login view.
 * Handles user authentication via PIN and manages animated UI transitions.
 */
public class LoginController implements Initializable {

    private static final Logger logger = LogManager.getLogger(LoginController.class);
    private static final String PREFIX = "[LOGIN-VIEW]";

    private final UsersRepository usersRepo = new UsersRepositoryImpl();

    @FXML private AnchorPane leftPane;
    @FXML private Label loginTitle;
    @FXML private Label subtitleLabel;
    @FXML private ImageView logoImageView;
    @FXML private Label loginLabel;
    @FXML private PasswordField pinField;

    // ========================================================================
    //                            DYNAMIC TEXT
    // ========================================================================

    /** Returns a greeting message based on the current hour. */
    private String getGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour >= 6 && hour < 12) return "¡Buenos días!";
        else if (hour >= 12 && hour < 19) return "¡Buenas tardes!";
        else return "¡Buenas noches!";
    }

    // ========================================================================
    //                            INITIALIZATION
    // ========================================================================

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.info("{} Initializing login view...", PREFIX);

        // Background gradient setup
        leftPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #3a3a3a, #232323);");

        // Dynamic greetings
        loginTitle.setText(getGreeting());
        subtitleLabel.setText(PhraseLoaderUtil.getRandomLoginPhrase());

        // PIN input restriction (4 digits max)
        pinField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            return newText.matches("\\d{0,4}") ? change : null;
        }));

        // Focus on PIN field at start
        Platform.runLater(() -> {
            pinField.requestFocus();
            pinField.positionCaret(pinField.getText().length()); // cursor al final, por si acaso
        });

        // Enter key triggers login
        pinField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> loginController();   // Enter calls login
                default -> {} // inores other keys
            }
        });

        // Wait a bit to let window finish rendering (avoids cachedNode issues)
        Platform.runLater(() -> {
            PauseTransition wait = new PauseTransition(Duration.millis(350));
            wait.setOnFinished(e -> playIntroAnimations());
            wait.play();
        });

        // Setup bounce interaction on logo
        Platform.runLater(this::setupLogoBounce);

        logger.info("{} Login view initialized successfully.", PREFIX);
    }

    // ========================================================================
    //                           ANIMATIONS
    // ========================================================================

    /** Plays the initial fade-in and scale animations when the view loads. */
    private void playIntroAnimations() {
        Duration fadeDur = Duration.millis(400);
        Duration logoDur = Duration.millis(800);

        // --- TITLE FADE-IN ---
        FadeTransition fadeTitle = new FadeTransition(fadeDur, loginTitle);
        fadeTitle.setFromValue(0.0);
        fadeTitle.setToValue(1.0);

        // --- SUBTITLE FADE-IN ---
        FadeTransition fadeSubtitle = new FadeTransition(fadeDur, subtitleLabel);
        fadeSubtitle.setFromValue(0.0);
        fadeSubtitle.setToValue(1.0);

        // Both labels fade simultaneously
        ParallelTransition labelsAnim = new ParallelTransition(fadeTitle, fadeSubtitle);

        // --- LOGO APPEAR + SCALE ---
        FadeTransition fadeLogo = new FadeTransition(logoDur, logoImageView);
        fadeLogo.setFromValue(0.0);
        fadeLogo.setToValue(1.0);

        ScaleTransition scaleLogo = new ScaleTransition(logoDur.multiply(0.9), logoImageView);
        scaleLogo.setFromX(0.9);
        scaleLogo.setFromY(0.9);
        scaleLogo.setToX(1.0);
        scaleLogo.setToY(1.0);

        ParallelTransition logoAnim = new ParallelTransition(fadeLogo, scaleLogo);

        // Sequence: fade-in labels → small delay → logo entrance
        SequentialTransition sequence = new SequentialTransition(
                labelsAnim,
                new PauseTransition(Duration.millis(250)),
                logoAnim
        );

        sequence.play();
    }

    /** Adds a bounce feedback animation when the logo is clicked. */
    private void setupLogoBounce() {
        final long[] lastClick = {0};

        logoImageView.setOnMouseClicked(event -> {
            long now = System.currentTimeMillis();
            if (now - lastClick[0] < 300) return; // cooldown between clicks
            lastClick[0] = now;

            // Bounce (scale up then back)
            ScaleTransition bounce = new ScaleTransition(Duration.millis(280), logoImageView);
            bounce.setFromX(1.0);
            bounce.setFromY(1.0);
            bounce.setToX(1.10);
            bounce.setToY(1.10);
            bounce.setAutoReverse(true);
            bounce.setCycleCount(2);
            bounce.setInterpolator(Interpolator.SPLINE(0.25, 0.1, 0.25, 1.0));

            // Slight fade for tactile feel
            FadeTransition fade = new FadeTransition(Duration.millis(280), logoImageView);
            fade.setFromValue(1.0);
            fade.setToValue(0.93);
            fade.setAutoReverse(true);
            fade.setCycleCount(2);
            fade.setInterpolator(Interpolator.EASE_BOTH);

            new ParallelTransition(bounce, fade).play();
        });
    }

    // ========================================================================
    //                            LOGIN LOGIC
    // ========================================================================

    /** Handles user authentication via PIN. */
    @FXML
    public void loginController() {
        if (pinField.getText().length() != 4 || !pinField.getText().matches("\\d{4}")) {
            wrongPin();
            return;
        }

        loginLabel.setVisible(false);
        pinField.getStyleClass().remove("error");

        String PIN = pinField.getText();
        User session = usersRepo.findByPin(PIN);

        if (session != null && session.getPin().equals(PIN)) {
            logger.info("{} Valid PIN. Starting session for user '{}'.", PREFIX, session.getName());
            SessionManager.startSession(session);
            Stage stage = (Stage) pinField.getScene().getWindow();
            WindowManager.switchWindow(stage, "/app/barbman/core/view/main-view.fxml");
        } else {
            logger.warn("{} Invalid PIN entered: {}", PREFIX, PIN);
            wrongPin();
        }
    }

    /** Displays an error message and shakes the PIN field when authentication fails. */
    private void wrongPin() {
        loginLabel.setVisible(true);
        if (!pinField.getStyleClass().contains("error"))
            pinField.getStyleClass().add("error");

        TranslateTransition tt = new TranslateTransition(Duration.millis(50), pinField);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();

        logger.warn("{} Wrong PIN animation triggered.", PREFIX);
    }
}
