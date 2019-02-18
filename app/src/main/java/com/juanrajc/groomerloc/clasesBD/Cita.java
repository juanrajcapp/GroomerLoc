package com.juanrajc.groomerloc.clasesBD;

import java.util.Date;
import java.util.List;

public class Cita {

    private String idPeluquero, idCliente;
    private Float precioFinal;
    private Date fechaCreacion, fechaConfirmacion;
    private List<Integer> servicios;
    private Perro perro;

    public Cita(){
    }

    public Cita(String idPeluquero, String idCliente, Float precioFinal, Date fechaCreacion,
                Date fechaConfirmacion, List<Integer> servicios, Perro perro) {

        this.idPeluquero = idPeluquero;
        this.idCliente = idCliente;
        this.precioFinal = precioFinal;
        this.fechaCreacion = fechaCreacion;
        this.fechaConfirmacion = fechaConfirmacion;
        this.servicios = servicios;
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

    public List<Integer> getServicios() {
        return servicios;
    }

    public void setServicios(List<Integer> servicios) {
        this.servicios = servicios;
    }

    public Perro getPerro() {
        return perro;
    }

    public void setPerro(Perro perro) {
        this.perro = perro;
    }

}
