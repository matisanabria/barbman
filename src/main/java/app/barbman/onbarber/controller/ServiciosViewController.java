package app.barbman.onbarber.controller;

import app.barbman.onbarber.model.ServicioRealizado;
import app.barbman.onbarber.repository.ServicioRealizadoRepositoryImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ServiciosViewController implements Initializable {
    @FXML private TableView<ServicioRealizado> serviciosTable;
    @FXML private TableColumn<ServicioRealizado, Integer> colId;
    @FXML private TableColumn<ServicioRealizado, Integer> colBarberoId;
    @FXML private TableColumn<ServicioRealizado, Integer> colTipoServicio;
    @FXML private TableColumn<ServicioRealizado, Integer> colPrecio;
    @FXML private TableColumn<ServicioRealizado, java.util.Date> colFecha;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBarberoId.setCellValueFactory(new PropertyValueFactory<>("barberoId"));
        colTipoServicio.setCellValueFactory(new PropertyValueFactory<>("tipoServicio"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        mostrarServicios();
    }

    void mostrarServicios() {
        ServicioRealizadoRepositoryImpl repo = new ServicioRealizadoRepositoryImpl();
        List<ServicioRealizado> servicios = repo.findAll();
        if (servicios != null) {
            serviciosTable.setItems(FXCollections.observableArrayList(servicios));
        }
    }
}
