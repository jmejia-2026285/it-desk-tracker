package org.angelml.system.model;

public class Tecnico {

    private int id;
    private String nombre;
    private String email;
    private String password;
    private Rol rol = Rol.TECNICO;
    private boolean activo = true;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public boolean isAdmin() {
        return rol == Rol.ADMIN;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
