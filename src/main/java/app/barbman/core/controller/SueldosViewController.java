package app.barbman.core.controller;

import app.barbman.core.model.Barbero;
import app.barbman.core.model.Sueldo;
import app.barbman.core.repositories.barbero.BarberoRepository;
import app.barbman.core.repositories.barbero.BarberoRepositoryImpl;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepository;
import app.barbman.core.repositories.serviciorealizado.ServicioRealizadoRepositoryImpl;
import app.barbman.core.repositories.sueldos.SueldosRepository;
import app.barbman.core.repositories.sueldos.SueldosRepositoryImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class SueldosViewController implements Initializable {
    @FXML
    private TableView<Sueldo> sueldosTable;
    @FXML
    private TableColumn<Sueldo, String> colBarbero;
    @FXML
    private TableColumn<Sueldo, Integer> colProduccion;
    @FXML
    private TableColumn<Sueldo, Integer> colMonto;
    @FXML
    private TableColumn<Sueldo, String> colEstado;
    @FXML
    private TableColumn<Sueldo, String> colAccion;

    private final SueldosRepository sueldoRepo = new SueldosRepositoryImpl();
    private final ServicioRealizadoRepository repo = new ServicioRealizadoRepositoryImpl();
    private final BarberoRepository barberoRepo = new BarberoRepositoryImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Para que las columnas queden fijas
        sueldosTable.getColumns().forEach(col -> col.setReorderable(false));
        sueldosTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Configuración de las columnas de la tabla con las propiedades del modelo ServicioRealizad
        colBarbero.setCellValueFactory(cellData -> {
            int barberoId = cellData.getValue().getBarberoId();
            Barbero b = barberoRepo.findById(barberoId);
            String nombre = (b != null) ? b.getNombre() : "Desconocido";
            return new SimpleStringProperty(nombre);
        });
    }

}
