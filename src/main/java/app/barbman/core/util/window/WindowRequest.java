package app.barbman.core.util.window;

import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Describes how a JavaFX window should be opened.
 * This class is declarative: it contains no JavaFX logic.
 */
public class WindowRequest {
    // ============================================================
    // ======================== FIELDS ============================
    // ============================================================

    private final String fxmlPath;
    private final String title;
    private final Stage owner;
    private final boolean modal;
    private final boolean resizable;
    private final List<String> cssPaths;
    private final boolean returnController;

    // ============================================================
    // ===================== CONSTRUCTOR ==========================
    // ============================================================

    private WindowRequest(Builder builder) {
        this.fxmlPath = builder.fxmlPath;
        this.title = builder.title;
        this.owner = builder.owner;
        this.modal = builder.modal;
        this.resizable = builder.resizable;
        this.cssPaths = List.copyOf(builder.cssPaths);
        this.returnController = builder.returnController;
    }

    // ============================================================
    // ======================= GETTERS ============================
    // ============================================================

    public String getFxmlPath() {
        return fxmlPath;
    }

    public String getTitle() {
        return title;
    }

    public Stage getOwner() {
        return owner;
    }

    public boolean isModal() {
        return modal;
    }

    public boolean isResizable() {
        return resizable;
    }

    public List<String> getCssPaths() {
        return cssPaths;
    }

    public boolean shouldReturnController() {
        return returnController;
    }

    // ============================================================
    // ======================== BUILDER ===========================
    // ============================================================

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String fxmlPath;
        private String title;
        private Stage owner;
        private boolean modal = false;
        private boolean resizable = false;
        private final List<String> cssPaths = new ArrayList<>();
        private boolean returnController = false;

        /**
         * Required: FXML path for the window.
         */
        public Builder fxml(String fxmlPath) {
            this.fxmlPath = fxmlPath;
            return this;
        }

        /**
         * Optional window title.
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * Optional owner stage (used for modals).
         */
        public Builder owner(Stage owner) {
            this.owner = owner;
            return this;
        }

        /**
         * Marks this window as modal.
         */
        public Builder modal(boolean modal) {
            this.modal = modal;
            return this;
        }

        /**
         * Allows resizing the window.
         * Default is false.
         */
        public Builder resizable(boolean resizable) {
            this.resizable = resizable;
            return this;
        }

        /**
         * Adds a CSS stylesheet to this window.
         */
        public Builder css(String cssPath) {
            if (cssPath != null && !cssPath.isBlank()) {
                this.cssPaths.add(cssPath);
            }
            return this;
        }

        /**
         * Adds multiple CSS stylesheets.
         */
        public Builder css(List<String> cssPaths) {
            if (cssPaths != null) {
                cssPaths.stream()
                        .filter(p -> p != null && !p.isBlank())
                        .forEach(this.cssPaths::add);
            }
            return this;
        }

        /**
         * Indicates whether the controller should be returned.
         */
        public Builder returnController(boolean returnController) {
            this.returnController = returnController;
            return this;
        }

        /**
         * Builds the WindowRequest.
         */
        public WindowRequest build() {
            if (fxmlPath == null || fxmlPath.isBlank()) {
                throw new IllegalStateException("FXML path is required to open a window.");
            }
            return new WindowRequest(this);
        }
    }

    // ============================================================
    // ======================== DEBUG =============================
    // ============================================================

    @Override
    public String toString() {
        return "WindowRequest{" +
                "fxmlPath='" + fxmlPath + '\'' +
                ", title='" + title + '\'' +
                ", modal=" + modal +
                ", resizable=" + resizable +
                ", cssPaths=" + cssPaths +
                ", returnController=" + returnController +
                '}';
    }
}
