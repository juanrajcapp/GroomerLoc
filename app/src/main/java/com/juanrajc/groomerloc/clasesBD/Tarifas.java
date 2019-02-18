package com.juanrajc.groomerloc.clasesBD;

public class Tarifas {

    private Float baseBanio, extraBanio, baseArreglo, extraArreglo, baseCorte, extraCorte,
            baseDeslanado, extraDeslanado, baseTinte, extraTinte, pesoExtra, precioOidos,
            precioUnias, precioAnales;

    public Tarifas(){
    }

    public Tarifas(Float baseBanio, Float extraBanio, Float baseArreglo, Float extraArreglo,
                   Float baseCorte, Float extraCorte, Float baseDeslanado, Float extraDeslanado,
                   Float baseTinte, Float extraTinte, Float pesoExtra, Float precioOidos,
                   Float precioUnias, Float precioAnales) {

        this.baseBanio = baseBanio;
        this.extraBanio = extraBanio;
        this.baseArreglo = baseArreglo;
        this.extraArreglo = extraArreglo;
        this.baseCorte = baseCorte;
        this.extraCorte = extraCorte;
        this.baseDeslanado = baseDeslanado;
        this.extraDeslanado = extraDeslanado;
        this.baseTinte = baseTinte;
        this.extraTinte = extraTinte;
        this.pesoExtra = pesoExtra;
        this.precioOidos = precioOidos;
        this.precioUnias = precioUnias;
        this.precioAnales = precioAnales;

    }

    public Float getBaseBanio() {
        return baseBanio;
    }

    public void setBaseBanio(Float baseBanio) {
        this.baseBanio = baseBanio;
    }

    public Float getExtraBanio() {
        return extraBanio;
    }

    public void setExtraBanio(Float extraBanio) {
        this.extraBanio = extraBanio;
    }

    public Float getBaseArreglo() {
        return baseArreglo;
    }

    public void setBaseArreglo(Float baseArreglo) {
        this.baseArreglo = baseArreglo;
    }

    public Float getExtraArreglo() {
        return extraArreglo;
    }

    public void setExtraArreglo(Float extraArreglo) {
        this.extraArreglo = extraArreglo;
    }

    public Float getBaseCorte() {
        return baseCorte;
    }

    public void setBaseCorte(Float baseCorte) {
        this.baseCorte = baseCorte;
    }

    public Float getExtraCorte() {
        return extraCorte;
    }

    public void setExtraCorte(Float extraCorte) {
        this.extraCorte = extraCorte;
    }

    public Float getBaseDeslanado() {
        return baseDeslanado;
    }

    public void setBaseDeslanado(Float baseDeslanado) {
        this.baseDeslanado = baseDeslanado;
    }

    public Float getExtraDeslanado() {
        return extraDeslanado;
    }

    public void setExtraDeslanado(Float extraDeslanado) {
        this.extraDeslanado = extraDeslanado;
    }

    public Float getBaseTinte() {
        return baseTinte;
    }

    public void setBaseTinte(Float baseTinte) {
        this.baseTinte = baseTinte;
    }

    public Float getExtraTinte() {
        return extraTinte;
    }

    public void setExtraTinte(Float extraTinte) {
        this.extraTinte = extraTinte;
    }

    public Float getPesoExtra() {
        return pesoExtra;
    }

    public void setPesoExtra(Float pesoExtra) {
        this.pesoExtra = pesoExtra;
    }

    public Float getPrecioOidos() {
        return precioOidos;
    }

    public void setPrecioOidos(Float precioOidos) {
        this.precioOidos = precioOidos;
    }

    public Float getPrecioUnias() {
        return precioUnias;
    }

    public void setPrecioUnias(Float precioUnias) {
        this.precioUnias = precioUnias;
    }

    public Float getPrecioAnales() {
        return precioAnales;
    }

    public void setPrecioAnales(Float precioAnales) {
        this.precioAnales = precioAnales;
    }

}
