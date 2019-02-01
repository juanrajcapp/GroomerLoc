package com.juanrajc.groomerloc.clasesBD;

import java.util.Date;

public class Cita {

    private String idPeluquero, idCliente, servicio;
    private Float precioFinal;
    private Date fechaCreacion, fechaConfirmacion;
    private Perro perro;

    public Cita(){
    }

    public Cita(String idPeluquero, String idCliente, String servicio, Float precioFinal,
                Date fechaCreacion, Date fechaConfirmacion, Perro perro) {

        this.idPeluquero = idPeluquero;
        this.idCliente = idCliente;
        this.servicio = servicio;
        this.precioFinal = precioFinal;
        this.fechaCreacion = fechaCreacion;
        this.fechaConfirmacion = fechaConfirmacion;
        this.perro = perro;

    }

    public String getIdPeluquero() {
        return idPeluquero;
    }

    public void setIdPeluquero(String idPeluquero) {
        this.idPeluquero = idPeluquero;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getServicio() {
        return servicio;
    }

    public void setServicio(String servicio) {
        this.servicio = servicio;
    }

    public Float getPrecioFinal() {
        return precioFinal;
    }

    public void setPrecioFinal(Float precioFinal) {
        this.precioFinal = precioFinal;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getFechaConfirmacion() {
        return fechaConfirmacion;
    }

    public void setFechaConfirmacion(Date fechaConfirmacion) {
        this.fechaConfirmacion = fechaConfirmacion;
    }

    public Perro getPerro() {
        return perro;
    }

    public void setPerro(Perro perro) {
        this.perro = perro;
    }

}
