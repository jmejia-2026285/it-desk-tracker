package org.angelml.system.repository;

import org.angelml.system.model.Cita;

import java.time.LocalDate;
import java.util.List;

public interface CitaInterface {

    List<Cita> findBetween(LocalDate from, LocalDate to, Integer tecnicoId);

    int save(Cita cita);

    void update(Cita cita);

    void setCompleted(int id, boolean completed);

    void delete(int id);

    int countByDate(LocalDate date, Integer tecnicoId);
}
