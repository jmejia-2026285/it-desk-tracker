package org.angelml.system.repository;

import org.angelml.system.model.Tecnico;

import java.util.List;
import java.util.Optional;

public interface TecnicoInterface {

    List<Tecnico> findAll();

    Optional<Tecnico> findById(int id);

    Optional<Tecnico> findByEmail(String email);

    int save(Tecnico tecnico);

    void update(Tecnico tecnico);

    void setActive(int id, boolean active);

    int count();

    int countActiveAdmins();
}
