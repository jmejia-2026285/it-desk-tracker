package org.angelml.system.model;

import java.time.LocalDateTime;

public class Ticket {

    private int id;
    private int dispositivoId;
    private Integer tecnicoId;
    private String folio;
    private String descripcion;
    private EstadoServicio estado = EstadoServicio.PENDIENTE;
    private LocalDateTime fechaIngreso;
    private LocalDateTime fechaCompletado;
    private boolean impreso;
    private LocalDateTime fechaImpresion;
    private String dispositivoResumen;
    private String cliente;
    private String tecnicoNombre;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getDispositivoId() { return dispositivoId; }
    public void setDispositivoId(int dispositivoId) { this.dispositivoId = dispositivoId; }
    public Integer getTecnicoId() { return tecnicoId; }
    public void setTecnicoId(Integer tecnicoId) { this.tecnicoId = tecnicoId; }
    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public EstadoServicio getEstado() { return estado; }
    public void setEstado(EstadoServicio estado) { this.estado = estado; }
    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public LocalDateTime getFechaCompletado() { return fechaCompletado; }
    public void setFechaCompletado(LocalDateTime fechaCompletado) { this.fechaCompletado = fechaCompletado; }
    public boolean isImpreso() { return impreso; }
    public void setImpreso(boolean impreso) { this.impreso = impreso; }
    public LocalDateTime getFechaImpresion() { return fechaImpresion; }
    public void setFechaImpresion(LocalDateTime fechaImpresion) { this.fechaImpresion = fechaImpresion; }
    public String getDispositivoResumen() { return dispositivoResumen; }
    public void setDispositivoResumen(String dispositivoResumen) { this.dispositivoResumen = dispositivoResumen; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public String getTecnicoNombre() { return tecnicoNombre; }
    public void setTecnicoNombre(String tecnicoNombre) { this.tecnicoNombre = tecnicoNombre; }
}
