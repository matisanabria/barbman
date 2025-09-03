package app.barbman.onbarber.controller;

import app.barbman.onbarber.model.Barbero;
import app.barbman.onbarber.model.ServicioRealizado;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class EgresosViewController implements Initializable {
    @FXML
    private TableView<ServicioRealizado> egresosTable;

    @FXML
    private TableColumn<ServicioRealizado, String> colFecha;
    @FXML
    private TableColumn<ServicioRealizado, String> colTipo;
    @FXML
    private TableColumn<ServicioRealizado, String> colMonto;
    @FXML
    private TableColumn<ServicioRealizado, String> colDescripcion;

    @FXML
    private ChoiceBox<String> tipoEgresoBox;
    @FXML
    private TextField montoField;
    @FXML
    private TextField descripcionField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Para que las columnas queden fijas
        egresosTable.getColumns().forEach(col -> col.setReorderable(false));
        egresosTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Opciones de forma de pago
        tipoEgresoBox.setItems(FXCollections.observableArrayList(
                "egreso", "bono", "otro"
        ));
        tipoEgresoBox.getSelectionModel().selectFirst();
    }
}
