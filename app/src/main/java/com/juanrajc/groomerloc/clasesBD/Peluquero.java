package com.juanrajc.groomerloc.clasesBD;

import com.google.android.gms.maps.model.LatLng;

public class Peluquero {

    private int telefono;
    private LatLng loc;

    public Peluquero() {
    }

    public Peluquero(int telefono, LatLng loc){

        this.telefono=telefono;
        this.loc=loc;

    }

    public int getTelefono() {
        return telefono;
    }

    public void setTelefono(int telefono) {
        this.telefono = telefono;
    }

    public LatLng getLoc() {
        return loc;
    }

    public void setLoc(LatLng loc) {
        this.loc = loc;
    }
}
