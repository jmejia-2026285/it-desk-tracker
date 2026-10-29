package org.angelml.system.repository;

import org.angelml.system.model.Dispositivo;
import org.angelml.system.model.EstadoServicio;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DispositivoInterface {

    List<Dispositivo> findAll();

    Optional<Dispositivo> findById(int id);

    Dispositivo register(Dispositivo dispositivo);

    void update(Dispositivo dispositivo);

    void delete(int id);

    Map<EstadoServicio, Integer> countByStatus();
}
