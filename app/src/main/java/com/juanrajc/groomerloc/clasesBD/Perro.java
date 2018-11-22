package com.juanrajc.groomerloc.clasesBD;

public class Perro {

    private String nombre, raza, sexo, comentario;
    private float peso;

    public Perro() {
    }

    public Perro(String nombre, String raza, String sexo, String comentario, float peso) {
        this.nombre = nombre;
        this.raza = raza;
        this.sexo = sexo;
        this.comentario = comentario;
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

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public float getPeso() {
        return peso;
    }

    public void setPeso(float peso) {
        this.peso = peso;
    }
}