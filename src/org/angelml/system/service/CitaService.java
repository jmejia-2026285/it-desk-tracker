package org.angelml.system.service;

import org.angelml.system.model.Cita;
import org.angelml.system.repository.CitaInterface;
import org.angelml.system.repository.CitaRepository;
import org.angelml.system.utils.ValidationsUtils;

import java.time.LocalDate;
import java.util.List;

public class CitaService {

    public static final int MAX_TITLE = 100;
    public static final int MAX_DESCRIPTION = 500;

    private final CitaInterface repository;

    public CitaService() {
        this(new CitaRepository());
    }

    public CitaService(CitaInterface repository) {
        this.repository = repository;
    }

    public List<Cita> getBetween(LocalDate from, LocalDate to, Integer tecnicoId) {
        return repository.findBetween(from, to, tecnicoId);
    }

    public void save(Cita cita) {
        validate(cita);
        if (cita.getId() == 0) {
            cita.setId(repository.save(cita));
        } else {
            repository.update(cita);
        }
    }

    public void setCompleted(int id, boolean completed) {
        repository.setCompleted(id, completed);
    }

    public void delete(int id) {
        repository.delete(id);
    }

    public int countToday(Integer tecnicoId) {
        return repository.countByDate(LocalDate.now(), tecnicoId);
    }

    private void validate(Cita cita) {
        cita.setTitulo(ValidationsUtils.requireText(cita.getTitulo(), "título", MAX_TITLE));
        cita.setDescripcion(ValidationsUtils.limitText(cita.getDescripcion(), "descripción", MAX_DESCRIPTION));
        if (cita.getFecha() == null) {
            throw new IllegalArgumentException("Selecciona la fecha de la cita.");
        }
        if (cita.getHora() == null) {
            throw new IllegalArgumentException("Escribe la hora de la cita (formato 14:30).");
        }
        if (cita.getTecnicoId() <= 0) {
            throw new IllegalArgumentException("Selecciona el técnico de la cita.");
        }
    }
}
