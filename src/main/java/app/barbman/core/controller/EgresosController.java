package app.barbman.core.controller;

import app.barbman.core.model.Egreso;
import app.barbman.core.repositories.egresos.EgresosRepository;
import app.barbman.core.repositories.egresos.EgresosRepositoryImpl;
import app.barbman.core.service.egresos.EgresosService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class EgresosController implements Initializable {

    private static final Logger logger = LogManager.getLogger(EgresosController.class);
    // Formateador para mostrar precios sin decimales
    private final DecimalFormat formateadorNumeros = new DecimalFormat("#,###");
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
    @FXML
    private ChoiceBox<String> formaPagoBox;

    EgresosRepository egresosRepository = new EgresosRepositoryImpl();
    EgresosService egresosService = new EgresosService(egresosRepository);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.info("[EGRESOS-VIEW] Inicializando vista de egresos...");
        // Para que las columnas queden fijas
        egresosTable.getColumns().forEach(col -> col.setReorderable(false));
        egresosTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Configuración de las columnas de la tabla
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colMonto.setCellValueFactory(cellData -> {
            double precio = cellData.getValue().getMonto();
            return new SimpleStringProperty(formateadorNumeros.format(precio) +  " Gs");
        });
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        mostrarEgresos();

        // Opciones de forma de pago
        tipoEgresoBox.setItems(FXCollections.observableArrayList(
                "insumo", "servicio", "otros"
        ));
        tipoEgresoBox.setValue("insumo");
        formaPagoBox.setItems(FXCollections.observableArrayList(
                "efectivo", "transferencia"
        ));
        formaPagoBox.setValue("efectivo");
        tipoEgresoBox.getSelectionModel().selectFirst();
        guardarButton.setOnAction(e -> guardarEgreso());

        // Doble clic para borrar un egreso
        egresosTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !egresosTable.getSelectionModel().isEmpty()) {
                Egreso seleccionado = egresosTable.getSelectionModel().getSelectedItem();
                if (seleccionado != null) {
                    confirmarYBorrarEgreso(seleccionado);
                }
            }
        });
        // Formato automático de números en el campo monto
        montoField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isBlank()) {
                return;
            }
            // Quitar lo que no sea dígito
            String digits = newValue.replaceAll("[^\\d]", "");
            if (digits.isEmpty()) {
                montoField.setText("");
                return;
            }
            try {
                long valor = Long.parseLong(digits);
                String formateado = formateadorNumeros.format(valor);
                // Actualizar campo sin mover el cursor al inicio
                montoField.setText(formateado);
                montoField.positionCaret(formateado.length());
            } catch (NumberFormatException e) {
                logger.warn("[EGRESOS-VIEW] Valor no numérico en montoField: {}", newValue);
            }
        });

        logger.info("[EGRESOS-VIEW] Vista inicializada correctamente.");
    }

    /**
     * Carga y muestra los egresos en la tabla.
     * Los egresos se muestran en orden inverso (más recientes primero).
     */
    void mostrarEgresos() {
        logger.info("[EGRESOS-VIEW] Cargando lista de egresos...");
        List<Egreso> egresos = egresosRepository.findAll();
        Collections.reverse(egresos);
        egresosTable.setItems(FXCollections.observableArrayList(egresos));
        logger.info("[EGRESOS-VIEW] {} egresos cargados en la tabla.", egresos.size());
    }

    private void guardarEgreso() {
        String tipo = tipoEgresoBox.getValue();
        String descripcion = descripcionField.getText();
        String montoStr = montoField.getText().replace(".", "").trim();
        String formaPago = formaPagoBox.getValue();

        // Validación de campos vacíos
        if (tipo == null) {
            mostrarAlerta("Debe seleccionar un tipo de egreso.");
            logger.warn("[EGRESOS-VIEW] Validación fallida: tipo no seleccionado.");
            return;
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            mostrarAlerta("El campo 'Descripcion' es obligatorio.");
            logger.warn("[EGRESOS-VIEW] Validación fallida: descripción vacía.");
            return;
        }
        if (montoStr == null || montoStr.trim().isEmpty()) {
            mostrarAlerta("El campo 'Monto' es obligatorio.");
            logger.warn("[EGRESOS-VIEW] Validación fallida: monto vacío.");
            return;
        }
        double monto = Double.parseDouble(montoStr);
        egresosService.addEgreso(
                tipo,           // tipo
                monto, // monto
                descripcion,    // descripcion
                formaPago
        );
        logger.info("[EGRESOS-VIEW] Egreso agregado -> Tipo: {}, Monto: {}, FormaPago: {}, Descripción: {}",
                tipo, monto, formaPago, descripcion);
        mostrarEgresos();
    }

    private void mostrarAlerta(String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Validación");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void confirmarYBorrarEgreso(Egreso egreso) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Seguro que quieres eliminar este egreso?");
        confirm.setContentText(
                "Egreso ID: " + egreso.getId() +
                        "\nTipo: " + egreso.getTipo() +
                        "\nMonto: " + formateadorNumeros.format(egreso.getMonto()) + " Gs" +
                        "\nFecha: " + egreso.getFecha()
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                egresosRepository.delete(egreso.getId()); // <- delete en repo
                mostrarEgresos(); // refresca la tabla
                logger.info("[EGRESOS-VIEW] Egreso eliminado -> ID: {}, Tipo: {}, Monto: {}, Fecha: {}",
                        egreso.getId(), egreso.getTipo(), egreso.getMonto(), egreso.getFecha());
            }
            else{
                logger.info("[EGRESOS-VIEW] Cancelada eliminación de egreso -> ID: {}", egreso.getId());            }
        });
    }

}
