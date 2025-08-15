package app.barbman.onbarber.controller;

import app.barbman.onbarber.appsession.AppSession;
import app.barbman.onbarber.model.Barbero;
import app.barbman.onbarber.repositories.BarberoRepository;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Controlador para la vista de login.
 * Gestiona la autenticación mediante el PIN y la transición a la vista principal.
 */

public class LoginController {
    private static final Logger logger = LogManager.getLogger(LoginController.class);

    @FXML
    private Label loginLabel; // Label para mostrar mensajes de error
    @FXML
    private PasswordField pinField;


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
        Barbero sesion = BarberoRepository.getBarberoWithPin(PIN);

        // Si el barbero existe y el PIN coincide, iniciar sesión y abrir la vista principal
        if (sesion != null && sesion.getPin().equals(PIN)) {
            AppSession.iniciarSesion(sesion);
            try {
                // Cargar la vista principal main-view.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/barbman/onbarber/view/main-view.fxml"));
                Parent root = loader.load();
                // Crear y mostrar el nuevo Stage
                Stage mainStage = new Stage();
                mainStage.setScene(new Scene(root));
                mainStage.show();

                // Cerrar la ventana actual de login
                Stage actualStage = (Stage) pinField.getScene().getWindow();
                actualStage.close();
            } catch (IOException e) {
                logger.error("Error al abrir la vista principal: {}", e.getMessage());
            }
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
