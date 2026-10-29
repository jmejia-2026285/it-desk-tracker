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
import org.angelml.system.model.Dispositivo;
import org.angelml.system.model.EstadoServicio;
import org.angelml.system.model.Tecnico;
import org.angelml.system.model.Ticket;
import org.angelml.system.service.DispositivoService;
import org.angelml.system.service.TecnicoService;
import org.angelml.system.service.TicketService;
import org.angelml.system.utils.AlertUtils;
import org.angelml.system.utils.SceneManager;
import org.angelml.system.utils.SesionManager;
import org.angelml.system.utils.ValidationsUtils;

import java.time.LocalDateTime;

public class TicketController extends BaseController {

    @FXML
    private TextField txtBuscar;
    @FXML
    private ComboBox<EstadoServicio> cmbFiltroEstado, cmbNuevoEstado;
    @FXML
    private ComboBox<Tecnico> cmbFiltroTecnico;
    @FXML
    private Button btnNuevo, btnCambiarEstado, btnImprimir, btnEliminar;
    @FXML
    private TableView<Ticket> tblTickets;
    @FXML
    private TableColumn<Ticket, String> colFolio, colDispositivo, colCliente, colTecnico, colDescripcion, colImpreso;
    @FXML
    private TableColumn<Ticket, LocalDateTime> colIngreso, colCompletado;
    @FXML
    private TableColumn<Ticket, EstadoServicio> colEstado;
    @FXML
    private Label lblConteo;

    private final TicketService ticketService = new TicketService();
    private final DispositivoService dispositivoService = new DispositivoService();
    private final TecnicoService tecnicoService = new TecnicoService();
    private final ObservableList<Ticket> tickets = FXCollections.observableArrayList();
    private FilteredList<Ticket> filtered;

    @FXML
    private void initialize() {
        textColumn(colFolio, Ticket::getFolio);
        textColumn(colDispositivo, Ticket::getDispositivoResumen);
        textColumn(colCliente, Ticket::getCliente);
        textColumn(colTecnico, ticket -> ticket.getTecnicoNombre() == null ? "—" : ticket.getTecnicoNombre());
        textColumn(colDescripcion, Ticket::getDescripcion);
        textColumn(colImpreso, ticket -> ticket.isImpreso() ? "Sí" : "No");
        dateTimeColumn(colIngreso, Ticket::getFechaIngreso);
        dateTimeColumn(colCompletado, Ticket::getFechaCompletado);
        statusColumn(colEstado, Ticket::getEstado);

        filtered = new FilteredList<>(tickets, ticket -> true);
        SortedList<Ticket> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblTickets.comparatorProperty());
        tblTickets.setItems(sorted);

        cmbFiltroEstado.getItems().setAll(EstadoServicio.values());
        cmbNuevoEstado.getItems().setAll(EstadoServicio.values());
        cmbFiltroEstado.setButtonCell(placeholderCell("Todos los estados"));
        cmbFiltroTecnico.setButtonCell(placeholderCell("Todos los técnicos"));
        txtBuscar.textProperty().addListener((observable, before, now) -> applyFilters());
        cmbFiltroEstado.valueProperty().addListener((observable, before, now) -> applyFilters());
        cmbFiltroTecnico.valueProperty().addListener((observable, before, now) -> applyFilters());

        var noSelection = tblTickets.getSelectionModel().selectedItemProperty().isNull();
        btnCambiarEstado.disableProperty().bind(noSelection.or(cmbNuevoEstado.valueProperty().isNull()));
        btnImprimir.disableProperty().bind(noSelection);
        btnEliminar.disableProperty().bind(noSelection);
        btnEliminar.setVisible(SesionManager.isAdmin());
        btnEliminar.setManaged(SesionManager.isAdmin());

        loadTickets();
        runTask(tecnicoService::getAll, technicians -> cmbFiltroTecnico.getItems().setAll(technicians));
    }

    @FXML
    private void onNuevo() {
        SceneManager.showDialog(SceneManager.REG_TICKET, "Nuevo ticket", (RegTicketController controller) -> { });
        loadTickets();
    }

    @FXML
    private void onCambiarEstado() {
        Ticket selected = tblTickets.getSelectionModel().getSelectedItem();
        EstadoServicio status = cmbNuevoEstado.getValue();
        if (selected == null || status == null) {
            return;
        }
        runAction(() -> ticketService.changeStatus(selected.getId(), status), () -> {
            cmbNuevoEstado.setValue(null);
            loadTickets();
        });
    }

    @FXML
    private void onImprimir() {
        Ticket selected = tblTickets.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        runTask(() -> dispositivoService.getById(selected.getDispositivoId()), device -> {
            device.setFolio(selected.getFolio());
            device.setTicketId(selected.getId());
            SceneManager.showDialog(SceneManager.IMPRIMIR_TICKET, "Imprimir tickets",
                    (ImprimirTicketController controller) -> controller.setDevice(device));
            loadTickets();
        });
    }

    @FXML
    private void onEliminar() {
        Ticket selected = tblTickets.getSelectionModel().getSelectedItem();
        if (selected == null || !SesionManager.isAdmin()) {
            return;
        }
        if (AlertUtils.confirmDelete("Se eliminará el ticket " + selected.getFolio() + ". Esta acción no se puede deshacer.")) {
            runAction(() -> ticketService.delete(selected.getId()), this::loadTickets);
        }
    }

    private void loadTickets() {
        runTask(ticketService::getAll, result -> {
            tickets.setAll(result);
            applyFilters();
        });
    }

    private void applyFilters() {
        String text = ValidationsUtils.normalize(txtBuscar.getText()).trim();
        EstadoServicio status = cmbFiltroEstado.getValue();
        Tecnico technician = cmbFiltroTecnico.getValue();
        filtered.setPredicate(ticket -> (status == null || ticket.getEstado() == status)
                && (technician == null || (ticket.getTecnicoId() != null && ticket.getTecnicoId() == technician.getId()))
                && (text.isEmpty() || searchable(ticket).contains(text)));
        lblConteo.setText(filtered.size() == tickets.size()
                ? tickets.size() + " tickets"
                : filtered.size() + " de " + tickets.size() + " tickets");
    }

    private String searchable(Ticket ticket) {
        return ValidationsUtils.normalize(String.join(" ", ticket.getFolio(), ticket.getDispositivoResumen(),
                ticket.getCliente(), ticket.getDescripcion()));
    }

    private <T> ListCell<T> placeholderCell(String placeholder) {
        return new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? placeholder : item.toString());
            }
        };
    }
}
