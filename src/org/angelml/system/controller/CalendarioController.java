package org.angelml.system.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import org.angelml.system.model.Cita;
import org.angelml.system.model.Tecnico;
import org.angelml.system.service.CitaService;
import org.angelml.system.service.TecnicoService;
import org.angelml.system.utils.AlertUtils;
import org.angelml.system.utils.SesionManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

public class CalendarioController extends BaseController {

    private static final DateTimeFormatter DAY_TITLE = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale.forLanguageTag("es"));
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("EEE dd/MM", Locale.forLanguageTag("es"));
    private static final DateTimeFormatter TIME_INPUT = DateTimeFormatter.ofPattern("H:mm");
    private static final DateTimeFormatter TIME_OUTPUT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private ToggleButton tglDia, tglSemana;
    @FXML
    private DatePicker dpFecha, dpFechaCita;
    @FXML
    private Button btnHoy, btnNueva, btnGuardar, btnCompletar, btnEliminar;
    @FXML
    private Label lblRango, lblFormulario, lblSeleccion;
    @FXML
    private TableView<Cita> tblCitas;
    @FXML
    private TableColumn<Cita, String> colFecha, colHora, colTitulo, colTecnico;
    @FXML
    private TableColumn<Cita, Boolean> colEstado;
    @FXML
    private TextField txtTitulo, txtHora;
    @FXML
    private TextArea txaDescripcion;
    @FXML
    private ComboBox<Tecnico> cmbTecnico;

    private final CitaService citaService = new CitaService();
    private final TecnicoService tecnicoService = new TecnicoService();
    private final ObservableList<Cita> appointments = FXCollections.observableArrayList();
    private Cita editing;

    @FXML
    private void initialize() {
        limit(txtTitulo, CitaService.MAX_TITLE);
        limit(txaDescripcion, CitaService.MAX_DESCRIPTION);
        limit(txtHora, 5);

        ToggleGroup range = new ToggleGroup();
        tglDia.setToggleGroup(range);
        tglSemana.setToggleGroup(range);
        range.selectedToggleProperty().addListener((observable, before, now) -> {
            if (now == null) {
                before.setSelected(true);
            } else {
                loadAppointments();
            }
        });
        dateFormat(dpFecha);
        dateFormat(dpFechaCita);
        dpFecha.setValue(LocalDate.now());
        dpFecha.valueProperty().addListener((observable, before, now) -> {
            if (now == null) {
                dpFecha.setValue(before);
            } else {
                loadAppointments();
            }
        });

        textColumn(colFecha, cita -> SHORT_DATE.format(cita.getFecha()));
        textColumn(colHora, cita -> TIME_OUTPUT.format(cita.getHora()));
        textColumn(colTitulo, Cita::getTitulo);
        textColumn(colTecnico, Cita::getTecnicoNombre);
        colEstado.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().isCompletada()));
        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean completed, boolean empty) {
                super.updateItem(completed, empty);
                getStyleClass().removeAll("estado-pendiente", "estado-completado");
                if (empty || completed == null) {
                    setText(null);
                    return;
                }
                setText(completed ? "Completada" : "Pendiente");
                getStyleClass().add(completed ? "estado-completado" : "estado-pendiente");
            }
        });
        tblCitas.setItems(appointments);
        tblCitas.setPlaceholder(new Label("No hay citas en este periodo"));
        tblCitas.getSelectionModel().selectedItemProperty().addListener((observable, before, now) -> {
            if (now != null) {
                startEditing(now);
            }
        });

        boolean admin = SesionManager.isAdmin();
        cmbTecnico.setDisable(!admin);
        runTask(tecnicoService::getActive, technicians -> {
            cmbTecnico.getItems().setAll(technicians);
            selectCurrentTechnician();
        });
        newAppointment();
        loadAppointments();
    }

    @FXML
    private void onHoy() {
        dpFecha.setValue(LocalDate.now());
    }

    @FXML
    private void onNueva() {
        tblCitas.getSelectionModel().clearSelection();
        newAppointment();
    }

    @FXML
    private void onGuardar() {
        Cita cita = editing == null ? new Cita() : editing;
        Tecnico technician = cmbTecnico.getValue();
        cita.setTitulo(txtTitulo.getText());
        cita.setDescripcion(txaDescripcion.getText());
        cita.setFecha(dpFechaCita.getValue());
        cita.setTecnicoId(technician == null ? 0 : technician.getId());
        try {
            cita.setHora(LocalTime.parse(txtHora.getText() == null ? "" : txtHora.getText().trim(), TIME_INPUT));
        } catch (DateTimeParseException e) {
            AlertUtils.warning("Revisa los datos", "Escribe la hora con el formato 14:30.");
            return;
        }
        btnGuardar.setDisable(true);
        runAction(() -> citaService.save(cita), () -> {
            btnGuardar.setDisable(false);
            dpFecha.setValue(cita.getFecha());
            tblCitas.getSelectionModel().clearSelection();
            newAppointment();
            loadAppointments();
        });
    }

    @FXML
    private void onCompletar() {
        Cita selected = tblCitas.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        boolean completed = !selected.isCompletada();
        runAction(() -> citaService.setCompleted(selected.getId(), completed), () -> {
            tblCitas.getSelectionModel().clearSelection();
            newAppointment();
            loadAppointments();
        });
    }

    @FXML
    private void onEliminar() {
        Cita selected = tblCitas.getSelectionModel().getSelectedItem();
        if (selected == null || !AlertUtils.confirmDelete("Se eliminará la cita «" + selected.getTitulo() + "».")) {
            return;
        }
        runAction(() -> citaService.delete(selected.getId()), () -> {
            tblCitas.getSelectionModel().clearSelection();
            newAppointment();
            loadAppointments();
        });
    }

    private void loadAppointments() {
        LocalDate date = dpFecha.getValue();
        boolean week = tglSemana.isSelected();
        LocalDate from = week ? date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) : date;
        LocalDate to = week ? from.plusDays(6) : date;
        lblRango.setText(week
                ? "Semana del " + from.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es")))
                    + " al " + to.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es")))
                : capitalize(DAY_TITLE.format(date)));
        Integer tecnicoId = SesionManager.isAdmin() ? null : SesionManager.getTecnicoId();
        runTask(() -> citaService.getBetween(from, to, tecnicoId), appointments::setAll);
    }

    private void startEditing(Cita cita) {
        editing = cita;
        lblFormulario.setText("Editar cita");
        txtTitulo.setText(cita.getTitulo());
        txaDescripcion.setText(cita.getDescripcion());
        dpFechaCita.setValue(cita.getFecha());
        txtHora.setText(TIME_OUTPUT.format(cita.getHora()));
        cmbTecnico.getItems().stream()
                .filter(technician -> technician.getId() == cita.getTecnicoId())
                .findFirst()
                .ifPresent(cmbTecnico::setValue);
        btnCompletar.setText(cita.isCompletada() ? "Reabrir cita" : "Marcar completada");
        btnCompletar.setDisable(false);
        btnEliminar.setDisable(false);
        lblSeleccion.setText("Cita seleccionada de " + cita.getTecnicoNombre() + ".");
    }

    private void newAppointment() {
        editing = null;
        lblFormulario.setText("Nueva cita");
        txtTitulo.clear();
        txaDescripcion.clear();
        dpFechaCita.setValue(dpFecha.getValue());
        txtHora.clear();
        selectCurrentTechnician();
        btnCompletar.setText("Marcar completada");
        btnCompletar.setDisable(true);
        btnEliminar.setDisable(true);
        lblSeleccion.setText("");
    }

    private void selectCurrentTechnician() {
        cmbTecnico.getItems().stream()
                .filter(technician -> technician.getId() == SesionManager.getTecnicoId())
                .findFirst()
                .ifPresent(cmbTecnico::setValue);
    }

    private String capitalize(String text) {
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
