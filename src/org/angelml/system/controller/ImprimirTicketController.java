package org.angelml.system.controller;

import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.angelml.system.model.Dispositivo;
import org.angelml.system.service.TicketService;
import org.angelml.system.utils.AlertUtils;
import org.angelml.system.utils.SceneManager;
import org.angelml.system.utils.SesionManager;
import org.angelml.system.utils.TicketUtils;

public class ImprimirTicketController extends BaseController {

    private static final double PREVIEW_SCALE = 0.74;

    @FXML
    private HBox hbPreview;
    @FXML
    private Label lblFolio;
    @FXML
    private Button btnImprimir, btnCancelar;

    private final TicketService ticketService = new TicketService();
    private Dispositivo device;

    public void setDevice(Dispositivo device) {
        this.device = device;
        lblFolio.setText(device.getFolio() + "  ·  " + device.getCodigoInterno());
        hbPreview.getChildren().setAll(
                TicketUtils.preview(TicketUtils.generarTicketEquipo(device), PREVIEW_SCALE),
                TicketUtils.preview(TicketUtils.generarTicketCliente(device), PREVIEW_SCALE));
    }

    @FXML
    private void onCancelar() {
        SceneManager.closeDialog(btnCancelar);
    }

    @FXML
    private void onImprimir() {
        if (!TicketUtils.hasPrinters()) {
            AlertUtils.printFailed("No hay impresoras instaladas. Instala una (puede ser «Microsoft Print to PDF») e intenta de nuevo.");
            return;
        }
        PrinterJob job = TicketUtils.createPrintJob(btnImprimir.getScene().getWindow());
        if (job == null) {
            return;
        }
        Node equipmentTicket = TicketUtils.generarTicketEquipo(device);
        Node customerTicket = TicketUtils.generarTicketCliente(device);
        btnImprimir.setDisable(true);
        runTask(() -> {
            boolean printed = TicketUtils.imprimirTicketsEnHoja(job, equipmentTicket, customerTicket);
            if (printed && device.getTicketId() > 0) {
                ticketService.markPrinted(device.getTicketId(), SesionManager.getTecnicoId());
            }
            return printed;
        }, printed -> {
            btnImprimir.setDisable(false);
            if (printed) {
                AlertUtils.printed("Los dos tickets del folio " + device.getFolio() + " se enviaron a la impresora.");
                SceneManager.closeDialog(btnImprimir);
            } else {
                AlertUtils.printFailed("La impresora rechazó el trabajo de impresión.");
            }
        }, error -> {
            btnImprimir.setDisable(false);
            showFailure(error);
        });
    }
}
