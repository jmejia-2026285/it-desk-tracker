package org.angelml.system.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Cita {

    private int id;
    private int tecnicoId;
    private String titulo;
    private String descripcion;
    private LocalDate fecha;
    private LocalTime hora;
    private boolean completada;
    private String tecnicoNombre;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTecnicoId() { return tecnicoId; }
    public void setTecnicoId(int tecnicoId) { this.tecnicoId = tecnicoId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public boolean isCompletada() { return completada; }
    public void setCompletada(boolean completada) { this.completada = completada; }
    public String getTecnicoNombre() { return tecnicoNombre; }
    public void setTecnicoNombre(String tecnicoNombre) { this.tecnicoNombre = tecnicoNombre; }
}
