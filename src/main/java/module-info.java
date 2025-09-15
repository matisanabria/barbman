module app.barbman.core {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires org.apache.logging.log4j;
    requires java.desktop;
    requires javafx.base;

    opens app.barbman.core to javafx.fxml;
    opens app.barbman.core.controller to javafx.fxml;
    opens app.barbman.core.model to javafx.base;
    exports app.barbman.core;
}
