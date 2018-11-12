package com.juanrajc.groomerloc.clasesBD;

public class Mascota {

    private String nombre, raza;
    private float peso;

    public Mascota() {
    }

    public Mascota(String nombre, String raza, float peso) {
        this.nombre = nombre;
        this.raza = raza;
        this.peso = peso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public float getPeso() {
        return peso;
    }

    public void setPeso(float peso) {
        this.peso = peso;
    }
}
