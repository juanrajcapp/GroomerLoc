package com.juanrajc.groomerloc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CreaCitaActivity extends AppCompatActivity {

    //Objetos de las textView de la activity.
    private TextView tvCreaCitaPelu, tvCreaCitaFecha, tvCreaCitaMascota, tvCreaCitaPrecio;

    //Objetos de las CheckBox de la activity.
    private CheckBox cbCreaCitaBanio, cbCreaCitaArreglo,
            cbCreaCitaCorte, cbCreaCitaDeslanado, cbCreaCitaTinte,
            cbCreaCitaOidos, cbCreaCitaUnias, cbCreaCitaAnales;

    //Objetos de los botones de la activity.
    private Button bCreaCitaSelecMascota, bCreaCitaAtras, bCreaCitaConfirmar;

    //Objeto del círculo de carga.
    private ProgressBar circuloCargaCreaCita;

    //Datos necesarios para crear la cita.
    private String idPeluquero;
    private Date fechaCreacion;

    //Precios del pelquero.
    private float banio, banioExtra, arreglo, arregloExtra, corte,
            corteExtra, deslanado, deslanadoExtra, tinte, tinteExtra,
            oidos, unias, anales, pesoExtra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crea_cita);

        //Instancia del círculo de carga.
        circuloCargaCreaCita = findViewById(R.id.circuloCargaCreaCita);

        //Instancias de los TextView de la activity.
        tvCreaCitaPelu = findViewById(R.id.tvCreaCitaPelu);
        tvCreaCitaFecha = findViewById(R.id.tvCreaCitaFecha);
        tvCreaCitaMascota = findViewById(R.id.tvCreaCitaMascota);
        tvCreaCitaPrecio = findViewById(R.id.tvCreCitaPrecio);

        //Instancias de los CheckBox de la activity.
        cbCreaCitaBanio = findViewById(R.id.cbCreaCitaBanio);
        cbCreaCitaArreglo = findViewById(R.id.cbCreaCitaArreglo);
        cbCreaCitaCorte = findViewById(R.id.cbCreaCitaCorte);
        cbCreaCitaDeslanado = findViewById(R.id.cbCreaCitaDeslanado);
        cbCreaCitaTinte = findViewById(R.id.cbCreaCitaTinte);
        cbCreaCitaOidos = findViewById(R.id.cbCreaCitaOidos);
        cbCreaCitaUnias = findViewById(R.id.cbCreaCitaUnias);
        cbCreaCitaAnales = findViewById(R.id.cbCreaCitaAnales);

        //Instancias de los botones de la activity.
        bCreaCitaSelecMascota = findViewById(R.id.bCreaCitaSelecMascota);
        bCreaCitaAtras = findViewById(R.id.bCreaCitaAtras);
        bCreaCitaConfirmar = findViewById(R.id.bCreaCitaConfirmar);
        /*Inician desactivados los botones de la activity. No se podrán usar hasta
        que los datos se hayan cargado.*/
        bCreaCitaSelecMascota.setEnabled(false);
        bCreaCitaAtras.setEnabled(false);
        bCreaCitaConfirmar.setEnabled(false);

        //Se visibiliza el círculo de carga.
        circuloCargaCreaCita.setVisibility(View.VISIBLE);

        //Obtiene y guarda la fecha y hora del momento en el que se creó la cita.
        fechaCreacion = Calendar.getInstance().getTime();

        //Obtiene y guarda la ID del peluquero al que se le va a pedir la cita.
        idPeluquero = getIntent().getStringExtra("idPeluquero");

        //Muestra en la activity el nombre del peluquero y la fecha de creación de la cita.
        tvCreaCitaPelu.setText(getIntent().getStringExtra("nombrePeluquero"));
        tvCreaCitaFecha.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(fechaCreacion));

        cargaOpciones();

    }

    /**
     * Método que carga las tarifas del peluquero, pasadas por Intent, y muestra las opciones disponibles.
     */
    private void cargaOpciones(){

        //Precios de baño.
        if(getIntent().hasExtra("banio")){

            banio = getIntent().getFloatExtra("banio", 0f);
            cbCreaCitaBanio.setVisibility(View.VISIBLE);

            if(getIntent().hasExtra("banioExtra")){
                banioExtra = getIntent().getFloatExtra("banioExtra", 0f);
            }

        }

        //Precios de arreglo o corte parcial.
        if(getIntent().hasExtra("arreglo")){

            arreglo = getIntent().getFloatExtra("arreglo", 0f);
            cbCreaCitaArreglo.setVisibility(View.VISIBLE);

            if(getIntent().hasExtra("arregloExtra")){
                arregloExtra = getIntent().getFloatExtra("arregloExtra", 0f);
            }

        }

        //Precios de corte completo.
        if(getIntent().hasExtra("corte")){

            corte = getIntent().getFloatExtra("corte", 0f);
            cbCreaCitaCorte.setVisibility(View.VISIBLE);

            if(getIntent().hasExtra("corteExtra")){
                corteExtra = getIntent().getFloatExtra("corteExtra", 0f);
            }

        }

        //Precios de deslanado.
        if(getIntent().hasExtra("deslanado")){

            deslanado = getIntent().getFloatExtra("deslanado", 0f);
            cbCreaCitaDeslanado.setVisibility(View.VISIBLE);

            if(getIntent().hasExtra("deslanadoExtra")){
                deslanadoExtra = getIntent().getFloatExtra("deslanadoExtra", 0f);
            }

        }

        //Precios de tinte.
        if(getIntent().hasExtra("tinte")){

            tinte = getIntent().getFloatExtra("tinte", 0f);
            cbCreaCitaTinte.setVisibility(View.VISIBLE);

            if(getIntent().hasExtra("tinteExtra")){
                tinteExtra = getIntent().getFloatExtra("tinteExtra", 0f);
            }

        }

        //Precio de limpieza de oidos.
        if(getIntent().hasExtra("oidos")){

            oidos = getIntent().getFloatExtra("oidos", 0f);
            cbCreaCitaOidos.setVisibility(View.VISIBLE);

        }

        //Precio de corte de uñas.
        if(getIntent().hasExtra("unias")){

            unias = getIntent().getFloatExtra("unias", 0f);
            cbCreaCitaUnias.setVisibility(View.VISIBLE);

        }

        //Precio de limpieza de glándulas anales.
        if(getIntent().hasExtra("anales")){

            anales = getIntent().getFloatExtra("anales", 0f);
            cbCreaCitaAnales.setVisibility(View.VISIBLE);

        }

        //Peso referencia para los extras.
        if(getIntent().hasExtra("pesoExtra")){
            pesoExtra = getIntent().getFloatExtra("pesoExtra", 0f);
        }

        //Finalizada la carga, se vuelve a invisibilizar el círculo de carga.
        circuloCargaCreaCita.setVisibility(View.INVISIBLE);

        //Activa los botones de selección de mascota y atrás.
        bCreaCitaSelecMascota.setEnabled(true);
        bCreaCitaAtras.setEnabled(true);

    }

    /**
     * Método que controla el retroceso a la activity anterior.
     *
     * @param view
     */
    protected void atras (View view){

        ////Se desactivan los botones de la activity para evitar varias pulsaciones simultáneas.
        bCreaCitaSelecMascota.setEnabled(false);
        bCreaCitaAtras.setEnabled(false);

        if(bCreaCitaConfirmar.isEnabled()){
            bCreaCitaConfirmar.setEnabled(false);
        }

        //Finaliza la activity.
        finish();

    }

}
