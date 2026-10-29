package org.angelml.system.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.angelml.system.model.Rol;
import org.angelml.system.model.Tecnico;
import org.angelml.system.service.TecnicoService;
import org.angelml.system.utils.AlertUtils;
import org.angelml.system.utils.SesionManager;

public class PersonalController extends BaseController {

    @FXML
    private TableView<Tecnico> tblTecnicos;
    @FXML
    private TableColumn<Tecnico, String> colNombre, colEmail, colRol, colEstado;
    @FXML
    private TextField txtNombre, txtEmail;
    @FXML
    private PasswordField pwdPassword;
    @FXML
    private ComboBox<Rol> cmbRol;
    @FXML
    private CheckBox chkActivo;
    @FXML
    private Button btnNuevo, btnGuardar;
    @FXML
    private Label lblFormulario, lblPasswordHint, lblConteo;

    private final TecnicoService tecnicoService = new TecnicoService();
    private final ObservableList<Tecnico> technicians = FXCollections.observableArrayList();
    private Tecnico editing;

    @FXML
    private void initialize() {
        if (!SesionManager.isAdmin()) {
            AlertUtils.accessDenied("Solo un administrador puede gestionar al personal.");
            tblTecnicos.setDisable(true);
            txtNombre.setDisable(true);
            txtEmail.setDisable(true);
            pwdPassword.setDisable(true);
            cmbRol.setDisable(true);
            chkActivo.setDisable(true);
            btnNuevo.setDisable(true);
            btnGuardar.setDisable(true);
            return;
        }
        limit(txtNombre, 100);
        limit(txtEmail, 120);
        textColumn(colNombre, Tecnico::getNombre);
        textColumn(colEmail, Tecnico::getEmail);
        textColumn(colRol, technician -> technician.getRol().toString());
        textColumn(colEstado, technician -> technician.isActivo() ? "Activo" : "Inactivo");
        tblTecnicos.setItems(technicians);
        cmbRol.getItems().setAll(Rol.values());
        tblTecnicos.getSelectionModel().selectedItemProperty().addListener((observable, before, now) -> {
            if (now != null) {
                startEditing(now);
            }
        });
        newTechnician();
        loadTechnicians();
    }

    @FXML
    private void onNuevo() {
        tblTecnicos.getSelectionModel().clearSelection();
        newTechnician();
    }

    @FXML
    private void onGuardar() {
        Tecnico technician = editing == null ? new Tecnico() : editing;
        technician.setNombre(txtNombre.getText());
        technician.setEmail(txtEmail.getText());
        technician.setRol(cmbRol.getValue());
        technician.setActivo(chkActivo.isSelected());
        String password = pwdPassword.getText();
        boolean creating = editing == null;
        if (!creating && technician.getId() == SesionManager.getTecnicoId() && !technician.isActivo()) {
            AlertUtils.warning("Revisa los datos", "No puedes desactivar tu propia cuenta.");
            return;
        }
        btnGuardar.setDisable(true);
        runTask(() -> {
            if (creating) {
                return tecnicoService.create(technician, password);
            }
            tecnicoService.update(technician, password);
            return technician;
        }, saved -> {
            btnGuardar.setDisable(false);
            tblTecnicos.getSelectionModel().clearSelection();
            newTechnician();
            loadTechnicians();
        }, error -> {
            btnGuardar.setDisable(false);
            showFailure(error);
        });
    }

    private void loadTechnicians() {
        runTask(tecnicoService::getAll, result -> {
            technicians.setAll(result);
            lblConteo.setText(result.size() + " técnicos");
        });
    }

    private void startEditing(Tecnico technician) {
        editing = technician;
        lblFormulario.setText("Editar técnico");
        txtNombre.setText(technician.getNombre());
        txtEmail.setText(technician.getEmail());
        pwdPassword.clear();
        cmbRol.setValue(technician.getRol());
        chkActivo.setSelected(technician.isActivo());
        lblPasswordHint.setText("Déjala vacía para conservar la contraseña actual.");
    }

    private void newTechnician() {
        editing = null;
        lblFormulario.setText("Nuevo técnico");
        txtNombre.clear();
        txtEmail.clear();
        pwdPassword.clear();
        cmbRol.setValue(Rol.TECNICO);
        chkActivo.setSelected(true);
        lblPasswordHint.setText("Mínimo 8 caracteres.");
    }
}
