package org.angelml.system.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.angelml.system.model.Dispositivo;
import org.angelml.system.service.DispositivoService;
import org.angelml.system.utils.ImagenUtils;

import java.io.File;
import java.util.List;

public abstract class DispositivoFormController extends BaseController {

    protected static final List<String> DEVICE_TYPES = List.of("Laptop", "PC de escritorio", "All-in-one", "Monitor",
            "Impresora", "Multifuncional", "Teléfono móvil", "Tablet", "Servidor", "Router / Switch", "UPS", "Otro");

    @FXML
    protected ComboBox<String> cmbTipo;
    @FXML
    protected TextField txtMarca, txtModelo, txtSerie, txtProblematica, txtCliente, txtTelefono, txtCorreo;
    @FXML
    protected TextArea txaDiagnostico, txaAccesorios, txaNotas;
    @FXML
    protected DatePicker dpFechaEstimada;
    @FXML
    protected StackPane pnlPreview;
    @FXML
    protected ImageView imgPreview;
    @FXML
    protected Label lblPreviewHint;

    protected File newImage;
    protected String currentImage;
    protected boolean imageRemoved;

    protected void setupForm() {
        cmbTipo.getItems().setAll(DEVICE_TYPES);
        limit(cmbTipo.getEditor(), DispositivoService.MAX_BRAND);
        limit(txtMarca, DispositivoService.MAX_BRAND);
        limit(txtModelo, DispositivoService.MAX_MODEL);
        limit(txtSerie, DispositivoService.MAX_SERIAL);
        limit(txtProblematica, DispositivoService.MAX_PROBLEM);
        limit(txtCliente, DispositivoService.MAX_NAME);
        limit(txtTelefono, DispositivoService.MAX_PHONE);
        limit(txtCorreo, DispositivoService.MAX_EMAIL);
        limit(txaDiagnostico, DispositivoService.MAX_DIAGNOSIS);
        limit(txaAccesorios, DispositivoService.MAX_LONG_TEXT);
        limit(txaNotas, DispositivoService.MAX_LONG_TEXT);
        dateFormat(dpFechaEstimada);
        pnlPreview.setOnMouseClicked(event -> chooseImage());
    }

    protected void readForm(Dispositivo target) {
        target.setTipo(cmbTipo.getEditor().getText());
        target.setMarca(txtMarca.getText());
        target.setModelo(txtModelo.getText());
        target.setSerie(txtSerie.getText());
        target.setProblematica(txtProblematica.getText());
        target.setDiagnostico(txaDiagnostico.getText());
        target.setAccesorios(txaAccesorios.getText());
        target.setCliente(txtCliente.getText());
        target.setTelefono(txtTelefono.getText());
        target.setCorreo(txtCorreo.getText());
        target.setFechaEstimada(dpFechaEstimada.getValue());
        target.setNotas(txaNotas.getText());
    }

    protected void fillForm(Dispositivo source) {
        cmbTipo.setValue(source.getTipo());
        txtMarca.setText(source.getMarca());
        txtModelo.setText(source.getModelo());
        txtSerie.setText(source.getSerie());
        txtProblematica.setText(source.getProblematica());
        txaDiagnostico.setText(source.getDiagnostico());
        txaAccesorios.setText(source.getAccesorios());
        txtCliente.setText(source.getCliente());
        txtTelefono.setText(source.getTelefono());
        txtCorreo.setText(source.getCorreo());
        dpFechaEstimada.setValue(source.getFechaEstimada());
        txaNotas.setText(source.getNotas());
        currentImage = source.getImagen();
        newImage = null;
        imageRemoved = false;
        showPreview(ImagenUtils.load(currentImage));
    }

    protected void clearForm() {
        cmbTipo.setValue(null);
        cmbTipo.getEditor().clear();
        for (TextField field : List.of(txtMarca, txtModelo, txtSerie, txtProblematica, txtCliente, txtTelefono, txtCorreo)) {
            field.clear();
        }
        for (TextArea area : List.of(txaDiagnostico, txaAccesorios, txaNotas)) {
            area.clear();
        }
        dpFechaEstimada.setValue(null);
        onLimpiarImagen();
        txtCliente.requestFocus();
    }

    @FXML
    protected void onLimpiarImagen() {
        imageRemoved = currentImage != null;
        newImage = null;
        showPreview(null);
    }

    private void chooseImage() {
        File file = ImagenUtils.chooseImage(pnlPreview.getScene().getWindow());
        if (file == null) {
            return;
        }
        newImage = file;
        imageRemoved = false;
        showPreview(new Image(file.toURI().toString(), 480, 320, true, true));
    }

    private void showPreview(Image image) {
        imgPreview.setImage(image);
        lblPreviewHint.setVisible(image == null);
        lblPreviewHint.setManaged(image == null);
    }
}
