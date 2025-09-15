package app.barbman.core.controller;

import app.barbman.core.model.CajaDiaria;
import app.barbman.core.service.caja.CajaService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

public class CierreCajaController implements Initializable {
    @FXML
    private Label lblFecha;
    @FXML private Label lblIngresos;
    @FXML private Label lblEgresos;
    @FXML private Label lblSaldoFinal;
    @FXML private Label lblEfectivo;
    @FXML private Label lblTransferencia;
    @FXML private Label lblPOS;

    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private static final Logger logger = LogManager.getLogger(CierreCajaController.class);

    private CajaDiaria cierre;
    private CajaService cajaService;
    private Runnable onGuardarCallback; // para avisar a CajaView que se guarde/refresque

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnCancelar.setOnAction(e -> onCancelar());
        btnGuardar.setOnAction(e -> onGuardar());
    }

    /**
     * Carga los datos del cierre en la vista de preview.
     */
    public void setData(CajaDiaria cierre, CajaService cajaService, Runnable onGuardarCallback) {
        this.cierre = cierre;
        this.cajaService = cajaService;
        this.onGuardarCallback = onGuardarCallback;

        lblFecha.setText("Fecha: " + cierre.getFecha());
        lblIngresos.setText("Ingresos totales: " + (int) cierre.getIngresosTotal() + " Gs");
        lblEgresos.setText("Egresos totales: " + (int) cierre.getEgresosTotal() + " Gs");
        lblSaldoFinal.setText("Saldo final: " + (int) cierre.getSaldoFinal() + " Gs");

        lblEfectivo.setText("- Efectivo: " + (int) cierre.getEfectivo() + " Gs");
        lblTransferencia.setText("- Transferencia: " + (int) cierre.getTransferencia() + " Gs");
        lblPOS.setText("- POS: " + (int) cierre.getPos() + " Gs");

        logger.info("Preview de cierre cargado para {}", cierre.getFecha());
    }

    private void onCancelar() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
        logger.info("Cierre de caja cancelado por el usuario.");
    }

    private void onGuardar() {
        try {
            cajaService.guardarCierre(cierre);
            logger.info("Cierre de caja guardado en DB para {}", cierre.getFecha());

            if (onGuardarCallback != null) {
                onGuardarCallback.run(); // avisamos al controller de CajaView que refresque
            }
        } catch (Exception e) {
            logger.error("Error al guardar cierre de caja: {}", e.getMessage(), e);
        }

        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        stage.close();
    }
}
