package app.barbman.core.controller;

import app.barbman.core.model.Barbero;
import app.barbman.core.repositories.barbero.BarberoRepository;
import app.barbman.core.repositories.barbero.BarberoRepositoryImpl;
import app.barbman.core.repositories.egresos.EgresosRepository;
import app.barbman.core.repositories.egresos.EgresosRepositoryImpl;
import app.barbman.core.service.egresos.EgresosService;
import app.barbman.core.util.AppSession;
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
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;

public class AdelantosController implements Initializable {
    @FXML
    private ChoiceBox<Barbero> barberoChoiceBox;
    @FXML
    private ChoiceBox<String> formaPagoChoiceBox;
    @FXML
    private TextField montoField;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnCancelar;

    private static final Logger logger = LogManager.getLogger(AdelantosController.class);

    private final BarberoRepository barberoRepository = new BarberoRepositoryImpl();
    private final EgresosRepository egresosRepository = new EgresosRepositoryImpl();
    private final EgresosService egresosService = new EgresosService(egresosRepository);

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
        logger.info("Vista de adelantos inicializada.");
        btnGuardar.setOnAction(e -> guardarAdelanto());
    }

    /**
     * Carga los barberos desde la base de datos en el ChoiceBox.
     * Además, selecciona automáticamente al barbero activo si está disponible en la sesión.
     */
    private void cargarBarberos() {
        List<Barbero> barberos = barberoRepository.findAll();
        barberoChoiceBox.setItems(FXCollections.observableArrayList(barberos));
        barberoChoiceBox.setConverter(new StringConverter<Barbero>() {
            @Override
            public String toString(Barbero b) {
                return (b == null) ? "" : b.getNombre();
            }

            @Override
            public Barbero fromString(String s) {
                return null;
            }
        });

        // Selecciona automáticamente el barbero activo si está en la lista
        Barbero activo = AppSession.getBarberoActivo();
        if (activo != null && barberos.contains(activo)) {
            barberoChoiceBox.setValue(activo);
            logger.info("Barbero activo preseleccionado: {}", activo.getNombre());
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
            Barbero barbero = barberoChoiceBox.getValue();
            if (barbero == null) {
                throw new IllegalArgumentException("Debe seleccionar un barbero.");
            }
            int barberoId = barbero.getId();

            double monto = Double.parseDouble(montoField.getText());
            String formaPago = formaPagoChoiceBox.getValue();

            egresosService.addAdelanto(barberoId, monto, formaPago);

            logger.info("Adelanto registrado correctamente -> Barbero: {}, Monto: {}, Forma de pago: {}",
                    barbero.getNombre(), monto, formaPago);

            // limpiar inputs
            montoField.clear();
            formaPagoChoiceBox.getSelectionModel().clearSelection();

        } catch (NumberFormatException e) {
            logger.error("Error al parsear el monto ingresado: {}", e.getMessage());
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
