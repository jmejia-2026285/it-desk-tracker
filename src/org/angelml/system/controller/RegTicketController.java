package org.angelml.system.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import org.angelml.system.model.Dispositivo;
import org.angelml.system.model.Tecnico;
import org.angelml.system.model.Ticket;
import org.angelml.system.service.DispositivoService;
import org.angelml.system.service.TecnicoService;
import org.angelml.system.service.TicketService;
import org.angelml.system.utils.AlertUtils;
import org.angelml.system.utils.SceneManager;
import org.angelml.system.utils.SesionManager;

import java.util.List;

public class RegTicketController extends BaseController {

    private record FormOptions(List<Dispositivo> devices, List<Tecnico> technicians) {
    }

    @FXML
    private ComboBox<Dispositivo> cmbDispositivo;
    @FXML
    private ComboBox<Tecnico> cmbTecnico;
    @FXML
    private TextArea txaDescripcion;
    @FXML
    private Button btnCancelar, btnCrear;

    private final TicketService ticketService = new TicketService();
    private final DispositivoService dispositivoService = new DispositivoService();
    private final TecnicoService tecnicoService = new TecnicoService();

    @FXML
    private void initialize() {
        limit(txaDescripcion, TicketService.MAX_DESCRIPTION);
        runTask(() -> new FormOptions(dispositivoService.getAll(), tecnicoService.getActive()), options -> {
            cmbDispositivo.getItems().setAll(options.devices());
            cmbTecnico.getItems().setAll(options.technicians());
            options.technicians().stream()
                    .filter(technician -> technician.getId() == SesionManager.getTecnicoId())
                    .findFirst()
                    .ifPresent(cmbTecnico::setValue);
        });
    }

    @FXML
    private void onCancelar() {
        SceneManager.closeDialog(btnCancelar);
    }

    @FXML
    private void onCrear() {
        Dispositivo device = cmbDispositivo.getValue();
        Tecnico technician = cmbTecnico.getValue();
        Ticket ticket = new Ticket();
        ticket.setDispositivoId(device == null ? 0 : device.getId());
        ticket.setTecnicoId(technician == null ? null : technician.getId());
        ticket.setDescripcion(txaDescripcion.getText());
        btnCrear.setDisable(true);
        runTask(() -> ticketService.create(ticket), saved -> {
            AlertUtils.success("Ticket creado", "Se creó el ticket " + saved.getFolio() + " para " + saved.getDispositivoResumen() + ".");
            SceneManager.closeDialog(btnCrear);
        }, error -> {
            btnCrear.setDisable(false);
            showFailure(error);
        });
    }
}
