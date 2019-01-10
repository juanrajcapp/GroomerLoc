package com.juanrajc.groomerloc.clasesBD;

public class Tarifas {

    private float baseBanio, extraBanio, baseoArreglo, extraArreglo, baseCorte, extraCorte,
            baseDeslanado, extraDeslanado, baseTinte, extraTinte, pesoExtra, precioOidos,
            precioUnias, precioAnales;

    public void Tarifas(){
    }

    public Tarifas(float baseBanio, float extraBanio, float baseoArreglo, float extraArreglo,
                   float baseCorte, float extraCorte, float baseDeslanado, float extraDeslanado,
                   float baseTinte, float extraTinte, float pesoExtra, float precioOidos,
                   float precioUnias, float precioAnales) {

        this.baseBanio = baseBanio;
        this.extraBanio = extraBanio;
        this.baseoArreglo = baseoArreglo;
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

    public float getBaseBanio() {
        return baseBanio;
    }

    public void setBaseBanio(float baseBanio) {
        this.baseBanio = baseBanio;
    }

    public float getExtraBanio() {
        return extraBanio;
    }

    public void setExtraBanio(float extraBanio) {
        this.extraBanio = extraBanio;
    }

    public float getBaseoArreglo() {
        return baseoArreglo;
    }

    public void setBaseoArreglo(float baseoArreglo) {
        this.baseoArreglo = baseoArreglo;
    }

    public float getExtraArreglo() {
        return extraArreglo;
    }

    public void setExtraArreglo(float extraArreglo) {
        this.extraArreglo = extraArreglo;
    }

    public float getBaseCorte() {
        return baseCorte;
    }

    public void setBaseCorte(float baseCorte) {
        this.baseCorte = baseCorte;
    }

    public float getExtraCorte() {
        return extraCorte;
    }

    public void setExtraCorte(float extraCorte) {
        this.extraCorte = extraCorte;
    }

    public float getBaseDeslanado() {
        return baseDeslanado;
    }

    public void setBaseDeslanado(float baseDeslanado) {
        this.baseDeslanado = baseDeslanado;
    }

    public float getExtraDeslanado() {
        return extraDeslanado;
    }

    public void setExtraDeslanado(float extraDeslanado) {
        this.extraDeslanado = extraDeslanado;
    }

    public float getBaseTinte() {
        return baseTinte;
    }

    public void setBaseTinte(float baseTinte) {
        this.baseTinte = baseTinte;
    }

    public float getExtraTinte() {
        return extraTinte;
    }

    public void setExtraTinte(float extraTinte) {
        this.extraTinte = extraTinte;
    }

    public float getPesoExtra() {
        return pesoExtra;
    }

    public void setPesoExtra(float pesoExtra) {
        this.pesoExtra = pesoExtra;
    }

    public float getPrecioOidos() {
        return precioOidos;
    }

    public void setPrecioOidos(float precioOidos) {
        this.precioOidos = precioOidos;
    }

    public float getPrecioUnias() {
        return precioUnias;
    }

    public void setPrecioUnias(float precioUnias) {
        this.precioUnias = precioUnias;
    }

    public float getPrecioAnales() {
        return precioAnales;
    }

    public void setPrecioAnales(float precioAnales) {
        this.precioAnales = precioAnales;
    }

}
