package app.barbman.core.controller;

import app.barbman.core.util.AppSession;
import app.barbman.core.model.Barbero;
import app.barbman.core.repositories.barbero.BarberoRepository;
import app.barbman.core.repositories.barbero.BarberoRepositoryImpl;
import app.barbman.core.util.WindowManager;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Controlador para la vista de login.
 * Gestiona la autenticación mediante el PIN y la transición a la vista principal.
 */

public class LoginController {
    private static final Logger logger = LogManager.getLogger(LoginController.class);
    private final BarberoRepository barberoRepo = new BarberoRepositoryImpl();

    @FXML
    private Label loginLabel; // Label para mostrar mensajes de error
    @FXML
    private PasswordField pinField;


    @FXML
    public void initialize() {
        // Limita a solo 4 dígitos
        pinField.setTextFormatter(new javafx.scene.control.TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            // Permitir solo si son dígitos y máximo 4 caracteres
            if (newText.matches("\\d{0,4}")) {
                return change;
            }
            return null;
        }));
        pinField.setOnAction(event -> loginController());
    }

    /**
     * Muestra mensaje de error y anima el campo de PIN
     * para indicar que el PIN es incorrecto.
     */
    public void wrongPin() {
        logger.info("PIN inválido");
        loginLabel.setVisible(true);
        if (!pinField.getStyleClass().contains("error")) {
            pinField.getStyleClass().add("error");
        }
        shake(pinField);
    }

    /**
     * Maneja el proceso de login.
     * Verifica el PIN y, si es válido, inicia la sesión y abre la vista principal.
     * Si el PIN es incorrecto, muestra el mensaje de error correspondiente.
     */
    @FXML
    public void loginController() {
        // Verificar que el PIN tenga 4 dígitos
        if (pinField.getText().length() != 4 || !pinField.getText().matches("\\d{4}")) {
            wrongPin();
            return;
        }
        // Limpiar el estilo de error si lo tiene
        logger.info("PIN válido. Iniciando sesión.");
        pinField.getStyleClass().remove("error");
        loginLabel.setVisible(false);

        // Buscar barbero con el PIN ingresado
        String PIN = pinField.getText();
        Barbero sesion = barberoRepo.findByPin(PIN);

        // Si el barbero existe y el PIN coincide, iniciar sesión y abrir la vista principal
        if (sesion != null && sesion.getPin().equals(PIN)) {
            AppSession.iniciarSesion(sesion);
            Stage stage = (Stage) pinField.getScene().getWindow();
            WindowManager.switchWindow(stage, "/app/barbman/core/view/main-view.fxml");
        } else {
            wrongPin();
        }
    }

    /**
     * Animación de sacudida para el campo de PIN.
     * Indica visualmente que el PIN ingresado es incorrecto.
     *
     * @param node Nodo al que se aplica la animación
     */
    private void shake(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }

}
