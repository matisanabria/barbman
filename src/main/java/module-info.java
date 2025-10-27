module app.barbman.core {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires org.apache.logging.log4j;
    requires java.desktop;

    opens app.barbman.core to javafx.fxml;
    opens app.barbman.core.controller to javafx.fxml;
    opens app.barbman.core.model to javafx.base;
    exports app.barbman.core;
    opens app.barbman.core.controller.salary to javafx.fxml;
}
