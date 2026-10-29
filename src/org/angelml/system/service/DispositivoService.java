package org.angelml.system.service;

import org.angelml.system.model.Dispositivo;
import org.angelml.system.model.EstadoServicio;
import org.angelml.system.repository.DispositivoInterface;
import org.angelml.system.repository.DispositivoRepository;
import org.angelml.system.utils.ValidationsUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class DispositivoService {

    public static final int MAX_NAME = 100;
    public static final int MAX_BRAND = 60;
    public static final int MAX_MODEL = 80;
    public static final int MAX_SERIAL = 80;
    public static final int MAX_PHONE = 20;
    public static final int MAX_EMAIL = 120;
    public static final int MAX_PROBLEM = 255;
    public static final int MAX_LONG_TEXT = 500;
    public static final int MAX_DIAGNOSIS = 1000;

    private final DispositivoInterface repository;

    public DispositivoService() {
        this(new DispositivoRepository());
    }

    public DispositivoService(DispositivoInterface repository) {
        this.repository = repository;
    }

    public List<Dispositivo> getAll() {
        return repository.findAll();
    }

    public Dispositivo getById(int id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("El dispositivo ya no existe."));
    }

    public Dispositivo register(Dispositivo dispositivo) {
        validate(dispositivo);
        if (dispositivo.getFechaEstimada() != null && dispositivo.getFechaEstimada().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha estimada de entrega no puede ser anterior a hoy.");
        }
        return repository.register(dispositivo);
    }

    public void update(Dispositivo dispositivo) {
        validate(dispositivo);
        repository.update(dispositivo);
    }

    public void delete(int id) {
        repository.delete(id);
    }

    public Map<EstadoServicio, Integer> countByStatus() {
        return repository.countByStatus();
    }

    private void validate(Dispositivo dispositivo) {
        dispositivo.setTipo(ValidationsUtils.requireText(dispositivo.getTipo(), "tipo de dispositivo", MAX_BRAND));
        dispositivo.setMarca(ValidationsUtils.requireText(dispositivo.getMarca(), "marca", MAX_BRAND));
        dispositivo.setModelo(ValidationsUtils.requireText(dispositivo.getModelo(), "modelo", MAX_MODEL));
        dispositivo.setSerie(ValidationsUtils.limitText(dispositivo.getSerie(), "número de serie", MAX_SERIAL));
        dispositivo.setCliente(ValidationsUtils.requireText(dispositivo.getCliente(), "nombre del cliente", MAX_NAME));
        dispositivo.setTelefono(ValidationsUtils.requireText(dispositivo.getTelefono(), "teléfono", MAX_PHONE));
        if (!ValidationsUtils.validatePhone(dispositivo.getTelefono())) {
            throw new IllegalArgumentException("El teléfono solo puede tener números, espacios, + - ( ) y al menos 7 dígitos.");
        }
        dispositivo.setCorreo(ValidationsUtils.limitText(dispositivo.getCorreo(), "correo", MAX_EMAIL));
        if (dispositivo.getCorreo() != null && !ValidationsUtils.validateEmail(dispositivo.getCorreo())) {
            throw new IllegalArgumentException("El correo no tiene un formato válido (ej. nombre@empresa.com).");
        }
        dispositivo.setProblematica(ValidationsUtils.requireText(dispositivo.getProblematica(), "problemática", MAX_PROBLEM));
        dispositivo.setDiagnostico(ValidationsUtils.limitText(dispositivo.getDiagnostico(), "diagnóstico", MAX_DIAGNOSIS));
        dispositivo.setAccesorios(ValidationsUtils.limitText(dispositivo.getAccesorios(), "accesorios", MAX_LONG_TEXT));
        dispositivo.setNotas(ValidationsUtils.limitText(dispositivo.getNotas(), "notas", MAX_LONG_TEXT));
    }
}
