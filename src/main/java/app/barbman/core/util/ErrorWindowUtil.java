package app.barbman.core.util;

import app.barbman.core.controller.FatalErrorController;
import app.barbman.core.util.window.WindowManager;
import app.barbman.core.util.window.WindowRequest;

/**
 * Semantic helper for fatal application errors.
 */
public class ErrorWindowUtil {

    private ErrorWindowUtil() {}

    public static void showFatalError(String message) {

        Object controller = WindowManager.showExclusive(
                WindowRequest.builder()
                        .fxml("/app/barbman/core/view/fatal-error-view.fxml")
                        .title("Error crítico")
                        .returnController(true)
                        .build()
        );

        if (controller instanceof FatalErrorController fatalController) {
            fatalController.setMessage(
                    message != null ? message : "Unknown fatal error."
            );
        }
    }
}
