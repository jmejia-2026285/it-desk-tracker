package org.angelml.system.service;

import org.angelml.system.model.EstadoServicio;
import org.angelml.system.model.Ticket;
import org.angelml.system.repository.TicketInterface;
import org.angelml.system.repository.TicketRepository;
import org.angelml.system.utils.ValidationsUtils;

import java.util.List;

public class TicketService {

    public static final int MAX_DESCRIPTION = 255;

    private final TicketInterface repository;

    public TicketService() {
        this(new TicketRepository());
    }

    public TicketService(TicketInterface repository) {
        this.repository = repository;
    }

    public List<Ticket> getAll() {
        return repository.findAll();
    }

    public Ticket create(Ticket ticket) {
        if (ticket.getDispositivoId() <= 0) {
            throw new IllegalArgumentException("Selecciona el dispositivo del ticket.");
        }
        ticket.setDescripcion(ValidationsUtils.requireText(ticket.getDescripcion(), "descripción", MAX_DESCRIPTION));
        return repository.save(ticket);
    }

    public void changeStatus(int id, EstadoServicio status) {
        Ticket current = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("El ticket ya no existe."));
        if (current.getEstado() == status) {
            throw new IllegalArgumentException("El ticket ya está en estado «" + status + "».");
        }
        repository.updateStatus(id, status);
    }

    public void markPrinted(int id, Integer tecnicoId) {
        repository.markPrinted(id, tecnicoId);
    }

    public void delete(int id) {
        repository.delete(id);
    }
}
