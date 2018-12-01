package com.juanrajc.groomerloc.recursos;

import android.net.Uri;

public class CardPerro {

    private String nombrePerro;
    private Uri fotoPerro;

    public CardPerro() {
    }

    public CardPerro(String nombrePerro, Uri fotoPerro) {
        this.nombrePerro = nombrePerro;
        this.fotoPerro = fotoPerro;
    }

    public String getNombrePerro() {
        return nombrePerro;
    }

    public void setNombrePerro(String nombrePerro) {
        this.nombrePerro = nombrePerro;
    }

    public Uri getFotoPerro() {
        return fotoPerro;
    }

    public void setFotoPerro(Uri fotoPerro) {
        this.fotoPerro = fotoPerro;
    }
}
