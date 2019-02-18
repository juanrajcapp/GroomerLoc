package com.juanrajc.groomerloc.clasesBD;

import com.juanrajc.groomerloc.recursos.MiLatLng;

import java.util.ArrayList;
import java.util.List;

public class Peluquero {

    private String nombre, locExtra;
    private List<String> nombresBusqueda;
    private long telefono;
    private MiLatLng loc;

    public Peluquero() {
    }

    public Peluquero(String nombre, long telefono, MiLatLng loc, String locExtra){

        this.nombre=nombre;
        this.telefono=telefono;
        this.loc=loc;
        this.locExtra=locExtra;

        creaNombresBusqueda(nombre);

    }

    public String getNombre() {
        return nombre;
    }

    public List<String> getNombresBusqueda(){
        return nombresBusqueda;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        creaNombresBusqueda(nombre);
    }

    public long getTelefono() {
        return telefono;
    }

    public void setTelefono(long telefono) {
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

    /**
     * Método que crea un registro de palabras clave provenientes del nombre del peluquero,
     * necesarias para la funcionalidad de búsqueda de peluqueros por parte del cliente.
     *
     * Todas las letras pasadas a minúscula:
     *      + Nombre completo y palabras separadas provenientes del nombre, mediante
     *        la referencia de los espacios de la misma:
     *              - Con caracteres especiales y acentos.
     *              - Sin caracteres especiales y acentos:
     *
     * @param nombre Nombre del peluquero.
     */
    private void creaNombresBusqueda(String nombre){

        this.nombresBusqueda = new ArrayList<String>();
        this.nombresBusqueda.add(nombre.toLowerCase());
        this.nombresBusqueda.add(eliminaEspecialesYAcentos(nombre.toLowerCase()));

        for(String elementos:nombre.toLowerCase().split(" ")){
            this.nombresBusqueda.add(elementos);
            this.nombresBusqueda.add(eliminaEspecialesYAcentos(elementos));
        }

    }

    /**
     * Método que elimina los caracteres especiales y acentos de una cadena.
     *
     * @param cadena Cadena de caracteres que se va a modificar.
     *
     * @return Cadena de caracteres ya modificada (sin caracteres especiales ni acentos).
     */
    private String eliminaEspecialesYAcentos(String cadena){

        // Cadena de caracteres original a sustituir.
        String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
        // Cadena de caracteres ASCII que reemplazarán los originales.
        String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC";
        String output = cadena;
        for (int i=0; i<original.length(); i++) {
            // Reemplazamos los caracteres especiales.
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }

        return output;

    }

}
