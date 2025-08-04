module app.barbman.onbarber {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires org.apache.logging.log4j;

    opens app.barbman.onbarber to javafx.fxml;
    opens app.barbman.onbarber.controller to javafx.fxml;
    exports app.barbman.onbarber;
}
