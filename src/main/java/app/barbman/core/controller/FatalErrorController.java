package app.barbman.core.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the fatal error screen.
 * Shows error details and allows the user to close the app.
 */
public class FatalErrorController implements Initializable {

    private static final Logger logger = LogManager.getLogger(FatalErrorController.class);

    @FXML private Label errorTitleLabel;
    @FXML private Label errorMessageLabel;
    @FXML private TextArea stackTraceArea;
    @FXML private Button closeButton;
    @FXML private Button copyButton;

    // Static field to hold the exception (set by Main before showing the view)
    private static Throwable lastException;

    public static void setLastException(Throwable throwable) {
        lastException = throwable;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.info("[FATAL-ERROR-VIEW] Initializing fatal error view");

        if (lastException != null) {
            displayException(lastException);
        } else {
            displayGenericError();
        }

        closeButton.setOnAction(e -> {
            logger.info("[FATAL-ERROR-VIEW] User clicked close, shutting down app");
            Platform.exit();
            System.exit(1);
        });

        if (copyButton != null) {
            copyButton.setOnAction(e -> copyStackTraceToClipboard());
        }
    }

    private void displayException(Throwable throwable) {
        // Set error message
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            message = throwable.getClass().getSimpleName();
        }

        errorTitleLabel.setText("Error crítico: " + throwable.getClass().getSimpleName());
        errorMessageLabel.setText(message);

        // Set stack trace
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        stackTraceArea.setText(sw.toString());
    }

    private void displayGenericError() {
        errorTitleLabel.setText("Error crítico");
        errorMessageLabel.setText("Ocurrió un error inesperado");
        stackTraceArea.setText("No hay detalles disponibles");
    }

    private void copyStackTraceToClipboard() {
        try {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(stackTraceArea.getText());
            clipboard.setContent(content);

            logger.info("[FATAL-ERROR-VIEW] Stack trace copied to clipboard");

            // Optional: Show a brief confirmation
            if (copyButton != null) {
                String originalText = copyButton.getText();
                copyButton.setText("✓ Copiado");
                copyButton.setDisable(true);

                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        Platform.runLater(() -> {
                            copyButton.setText(originalText);
                            copyButton.setDisable(false);
                        });
                    } catch (InterruptedException ignored) {}
                }).start();
            }
        } catch (Exception e) {
            logger.error("[FATAL-ERROR-VIEW] Failed to copy to clipboard", e);
        }
    }
}