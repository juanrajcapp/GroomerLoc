package com.juanrajc.groomerloc.clasesBD;

public class Cliente {

    private int telefono;
    private Mascota mascota;

    public Cliente() {
    }

    public Cliente(int telefono, Mascota mascota) {

        this.telefono = telefono;
        this.mascota = mascota;

    }

    public int getTelefono() {
        return telefono;
    }

    public void setTelefono(int telefono) {
        this.telefono = telefono;
    }

    public Mascota getMascota() {
        return mascota;
    }

    public void setMascota(Mascota mascota) {
        this.mascota = mascota;
    }
}
