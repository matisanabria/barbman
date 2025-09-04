package app.barbman.core.controller;

import app.barbman.core.model.Egreso;
import app.barbman.core.repositories.egresos.EgresosRepository;
import app.barbman.core.repositories.egresos.EgresosRepositoryImpl;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepository;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepositoryImpl;
import app.barbman.core.service.egresos.EgresosService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class EgresosViewController implements Initializable {
    @FXML
    private TableView<Egreso> egresosTable;

    @FXML
    private TableColumn<Egreso, String> colFecha;
    @FXML
    private TableColumn<Egreso, String> colTipo;
    @FXML
    private TableColumn<Egreso, String> colMonto;
    @FXML
    private TableColumn<Egreso, String> colDescripcion;

    @FXML
    private ChoiceBox<String> tipoEgresoBox;
    @FXML
    private TextField montoField;
    @FXML
    private TextField descripcionField;
    @FXML
    private Button guardarButton;

    EgresosRepository repo = new EgresosRepositoryImpl();
    EgresosService sr = new EgresosService(repo);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Para que las columnas queden fijas
        egresosTable.getColumns().forEach(col -> col.setReorderable(false));
        egresosTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Configuración de las columnas de la tabla
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        mostrarEgresos();

        // Opciones de forma de pago
        tipoEgresoBox.setItems(FXCollections.observableArrayList(
                "otros"
        ));
        tipoEgresoBox.getSelectionModel().selectFirst();
        guardarButton.setOnAction(e -> guardarEgreso());
    }

    /**
     * Carga y muestra los egresos en la tabla.
     * Los egresos se muestran en orden inverso (más recientes primero).
     */
    void mostrarEgresos() {
        List<Egreso> egresos = repo.findAll();
        Collections.reverse(egresos);
        egresosTable.setItems(FXCollections.observableArrayList(egresos));
    }

    private void guardarEgreso() {
        String tipo = tipoEgresoBox.getValue();
        String descripcion = descripcionField.getText();
        String montoStr = montoField.getText();

        // Validación de campos vacíos
        if (tipo == null) {
            mostrarAlerta("Debe seleccionar un tipo de egreso.");
            return;
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            mostrarAlerta("El campo 'Descripcion' es obligatorio.");
            return;
        }
        if (montoStr == null || montoStr.trim().isEmpty()) {
            mostrarAlerta("El campo 'Monto' es obligatorio.");
            return;
        }
        sr.addEgreso(
                tipo,           // tipo
                Double.parseDouble(montoStr), // monto
                descripcion    // descripcion
        );
        mostrarEgresos();
    }

    private void mostrarAlerta(String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Validación");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
