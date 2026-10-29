package org.angelml.system.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Dispositivo {

    private int id;
    private String tipo;
    private String marca;
    private String modelo;
    private String serie;
    private String cliente;
    private String telefono;
    private String correo;
    private String problematica;
    private String diagnostico;
    private String accesorios;
    private String notas;
    private String imagen;
    private EstadoServicio estado = EstadoServicio.PENDIENTE;
    private LocalDateTime fechaIngreso;
    private LocalDate fechaEstimada;
    private Integer tecnicoId;
    private String tecnicoNombre;
    private int ticketId;
    private String folio;

    public String getCodigoInterno() {
        return id == 0 ? "" : String.format("DSP-%04d", id);
    }

    public String getEquipo() {
        return marca + " " + modelo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getProblematica() { return problematica; }
    public void setProblematica(String problematica) { this.problematica = problematica; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getAccesorios() { return accesorios; }
    public void setAccesorios(String accesorios) { this.accesorios = accesorios; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public EstadoServicio getEstado() { return estado; }
    public void setEstado(EstadoServicio estado) { this.estado = estado; }
    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public LocalDate getFechaEstimada() { return fechaEstimada; }
    public void setFechaEstimada(LocalDate fechaEstimada) { this.fechaEstimada = fechaEstimada; }
    public Integer getTecnicoId() { return tecnicoId; }
    public void setTecnicoId(Integer tecnicoId) { this.tecnicoId = tecnicoId; }
    public String getTecnicoNombre() { return tecnicoNombre; }
    public void setTecnicoNombre(String tecnicoNombre) { this.tecnicoNombre = tecnicoNombre; }
    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }
    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }

    @Override
    public String toString() {
        return getCodigoInterno() + " · " + tipo + " " + getEquipo() + " · " + cliente;
    }
}
