module app.barbman.core {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires java.sql;
    requires java.net.http;
    requires org.apache.logging.log4j;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires com.sun.jna.platform;
    requires com.sun.jna;
    requires com.ibm.icu;

    // ORM
    requires org.hibernate.orm.core;
    requires jakarta.persistence;

    // Migrations
    requires org.flywaydb.core;
    opens db.migration to org.flywaydb.core;

    // DI
    requires com.google.guice;
    requires jakarta.inject;
    requires static lombok;

    // Lombok is annotation-processor only (compile-time), no requires needed.

    // JavaFX controllers
    opens app.barbman.core to javafx.fxml;
    opens app.barbman.core.controller to javafx.fxml;
    opens app.barbman.core.controller.salary to javafx.fxml;
    opens app.barbman.core.controller.cashbox to javafx.fxml;
    opens app.barbman.core.controller.sales to javafx.fxml;
    opens app.barbman.core.controller.appointments to javafx.fxml;

    // Hibernate needs deep reflection on entity packages
    opens app.barbman.core.model to org.hibernate.orm.core, javafx.base;
    opens app.barbman.core.model.human to org.hibernate.orm.core, javafx.base;
    opens app.barbman.core.model.sales to org.hibernate.orm.core, javafx.base;
    opens app.barbman.core.model.sales.products to org.hibernate.orm.core, javafx.base;
    opens app.barbman.core.model.sales.services to org.hibernate.orm.core, javafx.base;
    opens app.barbman.core.model.cashbox to org.hibernate.orm.core, javafx.base;
    opens app.barbman.core.model.salaries to org.hibernate.orm.core, javafx.base;
    opens app.barbman.core.model.time to org.hibernate.orm.core, javafx.base;
    opens app.barbman.core.infrastructure to org.hibernate.orm.core;

    // DTOs used in TableView/ObservableList need javafx.base
    opens app.barbman.core.dto.salecart to javafx.base;
    opens app.barbman.core.dto.history to javafx.base;

    exports app.barbman.core;
}
