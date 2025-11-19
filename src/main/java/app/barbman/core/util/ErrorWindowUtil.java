package app.barbman.core.util;

import app.barbman.core.controller.FatalErrorController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ErrorWindowUtil {

    public static void showFatalError(String message) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ErrorWindowUtil.class.getResource("/app/barbman/core/view/fatal-error-view.fxml")
            );
            Parent root = loader.load();

            FatalErrorController controller = loader.getController();
            controller.setMessage(message);

            Stage stage = new Stage();
            stage.setTitle("Fatal Error");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            // 1) Cerrar todo antes de mostrar
            WindowManager.closeAllWindows();

            // 2) Mostrar la ventana fatal como única sobreviviente
            stage.show();

        } catch (Exception e) {
            System.err.println("ERROR loading fatal-error-view.fxml");
            e.printStackTrace();
        }
    }

}
