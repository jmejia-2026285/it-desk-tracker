package org.angelml.system.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.angelml.system.model.Dispositivo;
import org.angelml.system.service.DispositivoService;
import org.angelml.system.utils.AlertUtils;
import org.angelml.system.utils.ImagenUtils;
import org.angelml.system.utils.SceneManager;

import java.io.File;
import java.io.IOException;

public class EditarDispositivoController extends DispositivoFormController {

    @FXML
    private Label lblCodigo;
    @FXML
    private Button btnGuardar, btnCancelar, btnImagen;

    private final DispositivoService dispositivoService = new DispositivoService();
    private Dispositivo device;

    @FXML
    private void initialize() {
        setupForm();
    }

    public void setDevice(Dispositivo device) {
        this.device = device;
        fillForm(device);
        lblCodigo.setText(device.getCodigoInterno() + "  ·  Folio " + device.getFolio() + "  ·  " + device.getEstado());
    }

    @FXML
    private void onCancelar() {
        SceneManager.navigate(SceneManager.DISPOSITIVOS);
    }

    @FXML
    private void onGuardar() {
        Dispositivo edited = new Dispositivo();
        readForm(edited);
        edited.setId(device.getId());
        edited.setImagen(imageRemoved ? null : currentImage);
        File image = newImage;
        String previousImage = currentImage;
        boolean replaceImage = image != null || imageRemoved;
        btnGuardar.setDisable(true);
        runTask(() -> {
            try {
                if (image != null) {
                    edited.setImagen(ImagenUtils.store(image));
                }
                dispositivoService.update(edited);
                if (replaceImage) {
                    ImagenUtils.delete(previousImage);
                }
                return edited;
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo guardar la imagen del dispositivo.", e);
            }
        }, saved -> {
            AlertUtils.success("Cambios guardados", "El dispositivo " + device.getCodigoInterno() + " se actualizó correctamente.");
            SceneManager.navigate(SceneManager.DISPOSITIVOS);
        }, error -> {
            btnGuardar.setDisable(false);
            showFailure(error);
        });
    }
}
