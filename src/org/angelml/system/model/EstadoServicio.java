package org.angelml.system.model;

public enum EstadoServicio {

    PENDIENTE("Pendiente"),
    EN_PROCESO("En proceso"),
    COMPLETADO("Completado");

    private final String label;

    EstadoServicio(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
