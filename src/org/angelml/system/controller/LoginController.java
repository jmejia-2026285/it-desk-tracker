package org.angelml.system.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.angelml.system.model.Tecnico;
import org.angelml.system.service.TecnicoService;
import org.angelml.system.utils.AnimationUtils;
import org.angelml.system.utils.SceneManager;
import org.angelml.system.utils.SesionManager;

public class LoginController extends BaseController {

    @FXML
    private Label lblTitulo, lblSubtitulo, lblMensaje;
    @FXML
    private TextField txtNombre, txtEmail;
    @FXML
    private PasswordField pwdPassword, pwdConfirmar;
    @FXML
    private VBox vbNombre, vbConfirmar;
    @FXML
    private Button btnIngresar;

    private final TecnicoService tecnicoService = new TecnicoService();
    private boolean setupMode;

    @FXML
    private void initialize() {
        btnIngresar.setDisable(true);
        runTask(tecnicoService::hasTechnicians, hasTechnicians -> {
            setupMode = !hasTechnicians;
            applyMode();
            btnIngresar.setDisable(false);
        }, error -> lblMensaje.setText(messageOf(error)));
    }

    @FXML
    private void onIngresar() {
        lblMensaje.setText("");
        String email = txtEmail.getText();
        String password = pwdPassword.getText();
        if (setupMode && !password.equals(pwdConfirmar.getText())) {
            fail("Las contraseñas no coinciden.");
            return;
        }
        btnIngresar.setDisable(true);
        runTask(() -> setupMode
                        ? tecnicoService.createFirstAdmin(txtNombre.getText(), email, password)
                        : tecnicoService.authenticate(email, password),
                this::openDashboard,
                error -> {
                    btnIngresar.setDisable(false);
                    fail(messageOf(error));
                });
    }

    private void openDashboard(Tecnico tecnico) {
        SesionManager.login(tecnico);
        SceneManager.showDashboard();
    }

    private void fail(String message) {
        lblMensaje.setText(message);
        AnimationUtils.shake(btnIngresar);
    }

    private void applyMode() {
        vbNombre.setVisible(setupMode);
        vbNombre.setManaged(setupMode);
        vbConfirmar.setVisible(setupMode);
        vbConfirmar.setManaged(setupMode);
        lblTitulo.setText(setupMode ? "Crear administrador" : "Iniciar sesión");
        lblSubtitulo.setText(setupMode
                ? "Aún no hay técnicos registrados. Crea la cuenta del administrador para comenzar."
                : "Ingresa con tu cuenta de técnico.");
        btnIngresar.setText(setupMode ? "Crear cuenta e ingresar" : "Ingresar");
    }
}
