package org.angelml.system.model;

public enum Rol {

    ADMIN("Administrador"),
    TECNICO("Técnico");

    private final String label;

    Rol(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
