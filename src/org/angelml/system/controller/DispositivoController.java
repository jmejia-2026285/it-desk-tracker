package org.angelml.system.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import org.angelml.system.model.Dispositivo;
import org.angelml.system.model.EstadoServicio;
import org.angelml.system.service.DispositivoService;
import org.angelml.system.utils.AlertUtils;
import org.angelml.system.utils.ImagenUtils;
import org.angelml.system.utils.SceneManager;
import org.angelml.system.utils.SesionManager;
import org.angelml.system.utils.ValidationsUtils;

import java.time.LocalDateTime;

public class DispositivoController extends BaseController {

    @FXML
    private TextField txtBuscar;
    @FXML
    private ComboBox<EstadoServicio> cmbEstado;
    @FXML
    private Button btnNuevo, btnEditar, btnImprimir, btnEliminar;
    @FXML
    private TableView<Dispositivo> tblDispositivos;
    @FXML
    private TableColumn<Dispositivo, String> colCodigo, colFolio, colTipo, colEquipo, colSerie, colCliente, colTelefono;
    @FXML
    private TableColumn<Dispositivo, LocalDateTime> colIngreso;
    @FXML
    private TableColumn<Dispositivo, EstadoServicio> colEstado;
    @FXML
    private Label lblConteo;

    private final DispositivoService dispositivoService = new DispositivoService();
    private final ObservableList<Dispositivo> devices = FXCollections.observableArrayList();
    private FilteredList<Dispositivo> filtered;

    @FXML
    private void initialize() {
        textColumn(colCodigo, Dispositivo::getCodigoInterno);
        textColumn(colFolio, Dispositivo::getFolio);
        textColumn(colTipo, Dispositivo::getTipo);
        textColumn(colEquipo, Dispositivo::getEquipo);
        textColumn(colSerie, device -> ValidationsUtils.isBlank(device.getSerie()) ? "—" : device.getSerie());
        textColumn(colCliente, Dispositivo::getCliente);
        textColumn(colTelefono, Dispositivo::getTelefono);
        dateTimeColumn(colIngreso, Dispositivo::getFechaIngreso);
        statusColumn(colEstado, Dispositivo::getEstado);

        filtered = new FilteredList<>(devices, device -> true);
        SortedList<Dispositivo> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblDispositivos.comparatorProperty());
        tblDispositivos.setItems(sorted);

        cmbEstado.getItems().setAll(EstadoServicio.values());
        cmbEstado.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(EstadoServicio status, boolean empty) {
                super.updateItem(status, empty);
                setText(empty || status == null ? "Todos los estados" : status.toString());
            }
        });
        txtBuscar.textProperty().addListener((observable, before, now) -> applyFilters());
        cmbEstado.valueProperty().addListener((observable, before, now) -> applyFilters());

        btnEditar.disableProperty().bind(tblDispositivos.getSelectionModel().selectedItemProperty().isNull());
        btnImprimir.disableProperty().bind(tblDispositivos.getSelectionModel().selectedItemProperty().isNull());
        btnEliminar.disableProperty().bind(tblDispositivos.getSelectionModel().selectedItemProperty().isNull());
        btnEliminar.setVisible(SesionManager.isAdmin());
        btnEliminar.setManaged(SesionManager.isAdmin());

        tblDispositivos.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                onEditar();
            }
        });
        loadDevices();
    }

    @FXML
    private void onNuevo() {
        SceneManager.navigate(SceneManager.REG_DISPOSITIVO);
    }

    @FXML
    private void onEditar() {
        Dispositivo selected = tblDispositivos.getSelectionModel().getSelectedItem();
        if (selected != null) {
            EditarDispositivoController controller = SceneManager.navigate(SceneManager.EDITAR_DISPOSITIVO);
            controller.setDevice(selected);
        }
    }

    @FXML
    private void onImprimir() {
        Dispositivo selected = tblDispositivos.getSelectionModel().getSelectedItem();
        if (selected != null) {
            SceneManager.showDialog(SceneManager.IMPRIMIR_TICKET, "Imprimir tickets",
                    (ImprimirTicketController controller) -> controller.setDevice(selected));
            loadDevices();
        }
    }

    @FXML
    private void onEliminar() {
        Dispositivo selected = tblDispositivos.getSelectionModel().getSelectedItem();
        if (selected == null || !SesionManager.isAdmin()) {
            return;
        }
        if (!AlertUtils.confirmDelete("Se eliminará " + selected.getCodigoInterno() + " (" + selected.getEquipo()
                + ") junto con todos sus tickets. Esta acción no se puede deshacer.")) {
            return;
        }
        runAction(() -> {
            dispositivoService.delete(selected.getId());
            ImagenUtils.delete(selected.getImagen());
        }, this::loadDevices);
    }

    private void loadDevices() {
        runTask(dispositivoService::getAll, result -> {
            devices.setAll(result);
            applyFilters();
        });
    }

    private void applyFilters() {
        String text = ValidationsUtils.normalize(txtBuscar.getText()).trim();
        EstadoServicio status = cmbEstado.getValue();
        filtered.setPredicate(device -> (status == null || device.getEstado() == status)
                && (text.isEmpty() || searchable(device).contains(text)));
        lblConteo.setText(filtered.size() == devices.size()
                ? devices.size() + " dispositivos"
                : filtered.size() + " de " + devices.size() + " dispositivos");
    }

    private String searchable(Dispositivo device) {
        return ValidationsUtils.normalize(String.join(" ", device.getCodigoInterno(), String.valueOf(device.getFolio()),
                device.getTipo(), device.getEquipo(), String.valueOf(device.getSerie()), device.getCliente(), device.getTelefono()));
    }
}
