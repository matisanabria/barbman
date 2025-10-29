package app.barbman.core.controller;

import app.barbman.core.model.User;
import app.barbman.core.repositories.expense.ExpenseRepositoryImpl;
import app.barbman.core.repositories.users.UsersRepository;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import app.barbman.core.repositories.expense.ExpenseRepository;
import app.barbman.core.service.egresos.EgresosService;
import app.barbman.core.util.NumberFormatterUtil;
import app.barbman.core.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdelantosController implements Initializable {
    @FXML
    private ChoiceBox<User> barberoChoiceBox;
    @FXML
    private ChoiceBox<String> formaPagoChoiceBox;
    @FXML
    private TextField montoField;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnCancelar;

    private static final Logger logger = LogManager.getLogger(AdelantosController.class);

    private final UsersRepository usersRepository = new UsersRepositoryImpl();
    private final ExpenseRepository expenseRepository = new ExpenseRepositoryImpl();
    private final EgresosService egresosService = new EgresosService(expenseRepository);

    /**
     * Inicializa la vista, cargando la lista de barberos y las formas de pago disponibles.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarBarberos();
        formaPagoChoiceBox.setItems(FXCollections.observableArrayList(
                "efectivo", "transferencia"
        ));
        formaPagoChoiceBox.setValue("efectivo");

        NumberFormatterUtil.applyToTextField(montoField);

        logger.info("Vista de adelantos inicializada.");
        btnGuardar.setOnAction(e -> guardarAdelanto());
    }

    /**
     * Carga los barberos desde la base de datos en el ChoiceBox.
     * Además, selecciona automáticamente al barbero activo si está disponible en la sesión.
     */
    private void cargarBarberos() {
        List<User> users = usersRepository.findAll();
        barberoChoiceBox.setItems(FXCollections.observableArrayList(users));
        barberoChoiceBox.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User b) {
                return (b == null) ? "" : b.getName();
            }

            @Override
            public User fromString(String s) {
                return null;
            }
        });

        // Selecciona automáticamente el barbero activo si está en la lista
        User activo = SessionManager.getActiveUser();
        if (activo != null && users.contains(activo)) {
            barberoChoiceBox.setValue(activo);
            logger.info("User activo preseleccionado: {}", activo.getName());
        }
        else {
            logger.warn("No se encontró barbero activo para preseleccionar.");
        }
    }

    /**
     * Maneja el evento del botón Cancelar.
     * Cierra la ventana actual.
     */
    @FXML
    private void onCancelar() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
        logger.info("Ventana de adelantos cerrada por el usuario.");
    }

    /**
     * Carga y guarda un nuevo adelanto en la base de datos.
     * Se valida la entrada desde los campos de la vista.
     */
    private void guardarAdelanto() {
        try {
            User user = barberoChoiceBox.getValue();
            if (user == null) {
                throw new IllegalArgumentException("Debe seleccionar un user.");
            }
            int barberoId = user.getId();

            double monto = Double.parseDouble(montoField.getText().replace(".", "").trim());
            String formaPago = formaPagoChoiceBox.getValue();

            egresosService.addAdelanto(barberoId, monto, formaPago);

            logger.info("Adelanto registrado correctamente -> User: {}, Monto: {}, Forma de pago: {}",
                    user.getName(), monto, formaPago);

            // limpiar inputs
            montoField.clear();
            formaPagoChoiceBox.getSelectionModel().clearSelection();

        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida al registrar adelanto: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado al registrar adelanto: {}", e.getMessage(), e);
        }
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
        logger.info("Ventana de adelantos cerrada al guardar.");
    }
}
