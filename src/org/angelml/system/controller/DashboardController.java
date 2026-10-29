package org.angelml.system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.angelml.system.model.EstadoServicio;
import org.angelml.system.model.Tecnico;
import org.angelml.system.service.CitaService;
import org.angelml.system.service.DispositivoService;
import org.angelml.system.utils.AlertUtils;
import org.angelml.system.utils.AnimationUtils;
import org.angelml.system.utils.SceneManager;
import org.angelml.system.utils.SesionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardController extends BaseController {

    private record HomeStats(Map<EstadoServicio, Integer> devices, int appointmentsToday) {
    }

    @FXML
    private StackPane contentHost;
    @FXML
    private VBox pnlInicio;
    @FXML
    private Button btnInicio, btnDispositivos, btnTickets, btnCalendario, btnPersonal, btnCerrarSesion;
    @FXML
    private VBox btnAccesoRegistrar, btnAccesoDispositivos, btnAccesoTickets, btnAccesoCalendario;
    @FXML
    private Label lblTecnico, lblRol, lblSaludo, lblFecha, lblPendientes, lblEnProceso, lblCompletados, lblCitasHoy;

    private final DispositivoService dispositivoService = new DispositivoService();
    private final CitaService citaService = new CitaService();
    private List<Button> navButtons;

    @FXML
    private void initialize() {
        Tecnico tecnico = SesionManager.getTecnico();
        SceneManager.setContentHost(contentHost);
        navButtons = List.of(btnInicio, btnDispositivos, btnTickets, btnCalendario, btnPersonal);

        lblTecnico.setText(tecnico.getNombre());
        lblRol.setText(tecnico.getRol().toString());
        lblSaludo.setText("Hola, " + tecnico.getNombre().split("\\s+")[0]);
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es")));
        lblFecha.setText(Character.toUpperCase(date.charAt(0)) + date.substring(1));
        btnPersonal.setVisible(tecnico.isAdmin());
        btnPersonal.setManaged(tecnico.isAdmin());

        showHome();
    }

    @FXML
    private void onNavegar(ActionEvent event) {
        navigate(event.getSource());
    }

    /** Clic en una tarjeta de acceso rápido (VBox con rol de botón). */
    @FXML
    private void onAcceso(MouseEvent event) {
        navigate(event.getSource());
    }

    /** Teclado: Enter o Espacio activan la tarjeta enfocada. */
    @FXML
    private void onAccesoKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
            navigate(event.getSource());
            event.consume();
        }
    }

    private void navigate(Object source) {
        if (source == btnInicio) {
            showHome();
        } else if (source == btnDispositivos || source == btnAccesoDispositivos) {
            open(SceneManager.DISPOSITIVOS, btnDispositivos);
        } else if (source == btnAccesoRegistrar) {
            open(SceneManager.REG_DISPOSITIVO, btnDispositivos);
        } else if (source == btnTickets || source == btnAccesoTickets) {
            open(SceneManager.TICKETS, btnTickets);
        } else if (source == btnCalendario || source == btnAccesoCalendario) {
            open(SceneManager.CALENDARIO, btnCalendario);
        } else if (source == btnPersonal) {
            if (SesionManager.isAdmin()) {
                open(SceneManager.PERSONAL, btnPersonal);
            } else {
                AlertUtils.accessDenied("Solo un administrador puede gestionar al personal.");
            }
        }
    }

    @FXML
    private void onCerrarSesion() {
        if (AlertUtils.confirm("Cerrar sesión", "¿Quieres cerrar tu sesión?")) {
            SesionManager.logout();
            SceneManager.showLogin();
        }
    }

    private void open(String viewName, Button active) {
        SceneManager.navigate(viewName);
        markActive(active);
    }

    private void showHome() {
        contentHost.getChildren().setAll(pnlInicio);
        AnimationUtils.fadeIn(pnlInicio);
        markActive(btnInicio);
        Integer tecnicoId = SesionManager.isAdmin() ? null : SesionManager.getTecnicoId();
        runTask(() -> new HomeStats(dispositivoService.countByStatus(), citaService.countToday(tecnicoId)), stats -> {
            lblPendientes.setText(String.valueOf(stats.devices().get(EstadoServicio.PENDIENTE)));
            lblEnProceso.setText(String.valueOf(stats.devices().get(EstadoServicio.EN_PROCESO)));
            lblCompletados.setText(String.valueOf(stats.devices().get(EstadoServicio.COMPLETADO)));
            lblCitasHoy.setText(String.valueOf(stats.appointmentsToday()));
        });
    }

    private void markActive(Button active) {
        for (Button button : navButtons) {
            button.getStyleClass().remove("nav-button-active");
        }
        active.getStyleClass().add("nav-button-active");
    }
}
