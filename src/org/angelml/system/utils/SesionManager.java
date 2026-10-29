package org.angelml.system.utils;

import org.angelml.system.model.Tecnico;

public final class SesionManager {

    private static Tecnico tecnico;

    private SesionManager() {
    }

    public static void login(Tecnico loggedTecnico) {
        tecnico = loggedTecnico;
    }

    public static void logout() {
        tecnico = null;
    }

    public static boolean isLoggedIn() {
        return tecnico != null;
    }

    public static Tecnico getTecnico() {
        return tecnico;
    }

    public static Integer getTecnicoId() {
        return tecnico == null ? null : tecnico.getId();
    }

    public static boolean isAdmin() {
        return tecnico != null && tecnico.isAdmin();
    }
}
