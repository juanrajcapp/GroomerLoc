package com.juanrajc.groomerloc.clasesBD;

public class Cliente {

    private String nombre;
    private long telefono;

    public Cliente() {
    }

    public Cliente(String nombre,long telefono) {

        this.nombre = nombre;
        this.telefono = telefono;

    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public long getTelefono() {
        return telefono;
    }

    public void setTelefono(long telefono) {
        this.telefono = telefono;
    }

}
