package com.juanrajc.groomerloc.clasesBD;

public class Cliente {

    private String nombre;
    private int telefono;

    public Cliente() {
    }

    public Cliente(String nombre,int telefono) {

        this.nombre = nombre;
        this.telefono = telefono;

    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTelefono() {
        return telefono;
    }

    public void setTelefono(int telefono) {
        this.telefono = telefono;
    }

}
