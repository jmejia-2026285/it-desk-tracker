package org.angelml.system.repository;

import org.angelml.system.model.EstadoServicio;
import org.angelml.system.model.Ticket;

import java.util.List;
import java.util.Optional;

public interface TicketInterface {

    List<Ticket> findAll();

    Optional<Ticket> findById(int id);

    Ticket save(Ticket ticket);

    void updateStatus(int id, EstadoServicio status);

    void markPrinted(int id, Integer tecnicoId);

    void delete(int id);
}
