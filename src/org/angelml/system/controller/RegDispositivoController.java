package org.angelml.system.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import org.angelml.system.model.Dispositivo;
import org.angelml.system.service.DispositivoService;
import org.angelml.system.utils.ImagenUtils;
import org.angelml.system.utils.SceneManager;
import org.angelml.system.utils.SesionManager;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public class RegDispositivoController extends DispositivoFormController {

    @FXML
    private Button btnRegistrar, btnLimpiar, btnLimpiarImagen;

    private final DispositivoService dispositivoService = new DispositivoService();

    @FXML
    private void initialize() {
        setupForm();
        dpFechaEstimada.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
    }

    @FXML
    private void onLimpiar() {
        clearForm();
    }

    @FXML
    private void onRegistrar() {
        Dispositivo device = new Dispositivo();
        readForm(device);
        device.setTecnicoId(SesionManager.getTecnicoId());
        File image = newImage;
        btnRegistrar.setDisable(true);
        runTask(() -> saveDevice(device, image), this::afterRegister, error -> {
            btnRegistrar.setDisable(false);
            showFailure(error);
        });
    }

    private Dispositivo saveDevice(Dispositivo device, File image) {
        String stored = null;
        try {
            if (image != null) {
                stored = ImagenUtils.store(image);
                device.setImagen(stored);
            }
            return dispositivoService.register(device);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la imagen del dispositivo.", e);
        } catch (RuntimeException e) {
            ImagenUtils.delete(stored);
            throw e;
        }
    }

    private void afterRegister(Dispositivo saved) {
        btnRegistrar.setDisable(false);
        clearForm();
        SceneManager.showDialog(SceneManager.IMPRIMIR_TICKET, "Imprimir tickets",
                (ImprimirTicketController controller) -> controller.setDevice(saved));
        SceneManager.navigate(SceneManager.DISPOSITIVOS);
    }
}
