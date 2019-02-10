package com.juanrajc.groomerloc.clasesBD;

import java.util.Date;

public class Mensaje {

    private String usuario, texto;
    private Date fecha;

    public Mensaje() {
    }

    public Mensaje(String usuario, String texto, Date fecha) {

        this.usuario = usuario;
        this.texto = texto;
        this.fecha = fecha;

    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

}