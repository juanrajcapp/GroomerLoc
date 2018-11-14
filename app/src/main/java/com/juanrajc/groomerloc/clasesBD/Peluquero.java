package com.juanrajc.groomerloc.clasesBD;


import com.juanrajc.groomerloc.recursos.MiLatLng;

public class Peluquero {

    private String nombre, locExtra;
    private int telefono;
    private MiLatLng loc;

    public Peluquero() {
    }

    public Peluquero(String nombre, int telefono, MiLatLng loc, String locExtra){

        this.nombre=nombre;
        this.telefono=telefono;
        this.loc=loc;
        this.locExtra=locExtra;

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

    public MiLatLng getLoc() {
        return loc;
    }

    public void setLoc(MiLatLng loc) {
        this.loc = loc;
    }

    public String getLocExtra() {
        return locExtra;
    }

    public void setLocExtra(String locExtra) {
        this.locExtra = locExtra;
    }
}
