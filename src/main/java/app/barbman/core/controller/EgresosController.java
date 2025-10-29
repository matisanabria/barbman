package app.barbman.core.controller;

import app.barbman.core.model.Expense;
import app.barbman.core.repositories.expense.ExpenseRepository;
import app.barbman.core.repositories.expense.ExpenseRepositoryImpl;
import app.barbman.core.service.egresos.EgresosService;
import app.barbman.core.util.NumberFormatterUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class EgresosController implements Initializable {

    private static final Logger logger = LogManager.getLogger(EgresosController.class);

    @FXML
    private TableView<Expense> egresosTable;
    @FXML
    private TableColumn<Expense, String> colFecha;
    @FXML
    private TableColumn<Expense, String> colTipo;
    @FXML
    private TableColumn<Expense, String> colMonto;
    @FXML
    private TableColumn<Expense, String> colFormaPago;
    @FXML
    private TableColumn<Expense, String> colDescripcion;

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

    ExpenseRepository expenseRepository = new ExpenseRepositoryImpl();
    EgresosService egresosService = new EgresosService(expenseRepository);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.info("[EGRESOS-VIEW] Inicializando vista de expense...");
        // Para que las columnas queden fijas
        egresosTable.getColumns().forEach(col -> col.setReorderable(false));
        egresosTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Configuración de las columnas de la tabla
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colMonto.setCellValueFactory(cellData -> {
            double precio = cellData.getValue().getAmount();
            return new SimpleStringProperty(NumberFormatterUtil.format(precio) +  " Gs");
        });
        colFormaPago.setCellValueFactory(cellData -> {
            int formaPago = cellData.getValue().getPaymentMethodId();
            return new SimpleStringProperty(formaPago != null ? formaPago : ""); // FIXME
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
                Expense seleccionado = egresosTable.getSelectionModel().getSelectedItem();
                if (seleccionado != null) {
                    confirmarYBorrarEgreso(seleccionado);
                }
            }
        });

        NumberFormatterUtil.applyToTextField(montoField);

        logger.info("[EGRESOS-VIEW] Vista inicializada correctamente.");
    }

    /**
     * Carga y muestra los expense en la tabla.
     * Los expense se muestran en orden inverso (más recientes primero).
     */
    void mostrarEgresos() {
        logger.info("[EGRESOS-VIEW] Cargando lista de expenses...");
        List<Expense> expenses = expenseRepository.findAll();
        Collections.reverse(expenses);
        egresosTable.setItems(FXCollections.observableArrayList(expenses));
        logger.info("[EGRESOS-VIEW] {} expenses cargados en la tabla.", expenses.size());
    }

    private void guardarEgreso() {
        String tipo = tipoEgresoBox.getValue();
        String descripcion = descripcionField.getText();
        String montoStr = montoField.getText().replace(".", "").trim();
        int formaPago = formaPagoBox.getValue();

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
        logger.info("[EGRESOS-VIEW] Expense agregado -> Tipo: {}, Monto: {}, FormaPago: {}, Descripción: {}",
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

    private void confirmarYBorrarEgreso(Expense expense) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Seguro que quieres eliminar este expense?");
        confirm.setContentText(
                "Expense ID: " + expense.getId() +
                        "\nTipo: " + expense.getType() +
                        "\nMonto: " + NumberFormatterUtil.format(expense.getAmount()) + " Gs" +
                        "\nFecha: " + expense.getDate()
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                expenseRepository.delete(expense.getId()); // <- delete en repo
                mostrarEgresos(); // refresca la tabla
                logger.info("[EGRESOS-VIEW] Expense eliminado -> ID: {}, Tipo: {}, Monto: {}, Fecha: {}",
                        expense.getId(), expense.getType(), expense.getAmount(), expense.getDate());
            }
            else{
                logger.info("[EGRESOS-VIEW] Cancelada eliminación de expense -> ID: {}", expense.getId());            }
        });
    }

}
