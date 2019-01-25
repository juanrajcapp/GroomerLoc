package com.juanrajc.groomerloc;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.juanrajc.groomerloc.clasesBD.Tarifas;

public class TarifasPeluActivity extends AppCompatActivity {

    //Tipo de moneda usada en las tarifas.
    private final String MONEDA=" €";

    //Objetos de los elementos de la vista.
    private TextView tvTarifasPelu, tvPesoTarifas, tvTaExtra, tvTaBanio,  tvTaBanioPrecio,
            tvTaBanioExtraPrecio, tvTaArreglo, tvTaArregloPrecio,
            tvTaArregloExtraPrecio, tvTaCompleto, tvTaCompletoPrecio,
            tvTaCompletoExtraPrecio, tvTaDeslanado, tvTaDeslanadoPrecio,
            tvTaDeslanadoExtraPrecio, tvTaTinte, tvTaTintePrecio,
            tvTaTinteExtraPrecio, tvTaOidos, tvTaOidosPrecio, tvTaUnias,
            tvTaUniasPrecio, tvTaAnales, tvTaAnalesPrecio;

    private Button bTarifasAtras, bTarifasPedirCita;

    //Objeto del círculo de carga.
    private ProgressBar circuloCargaTarPelu;

    //Objetos de Firebase (Autenticación y BD Firestore).
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    //Cadena con la ID y nombre del peluquero.
    private String idPeluquero, nombrePeluquero;

    //Intent que se pasará si el cliente decide crear una cita.
    private Intent intentCita;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarifas_pelu);

        //Instancia del círculo de carga.
        circuloCargaTarPelu = (ProgressBar) findViewById(R.id.circuloCargaTarPelu);

        instanciasElementosVista();

        //Instancias de la autenticación y la base de datos de Firebase.
        auth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();

        //Recoge y guarda el ID y nombre del peluquero.
        idPeluquero = getIntent().getStringExtra("idPeluquero");
        nombrePeluquero = getIntent().getStringExtra("nombrePeluquero");
        //Este último lo muestra en el título de la activity.
        tvTarifasPelu.setText(getString(R.string.tituloTarifasPelu)
                +" "+nombrePeluquero);

        //y se pasa al método que muestra los datos.
        cargaTarifas(idPeluquero);

    }

    /**
     * Método que se encarga de obtener las tarifas del peluquero seleccionado desde su BDen Firebase (si existen).
     */
    private void cargaTarifas(String idPeluquero){

        //Se visibiliza el círculo de carga.
        circuloCargaTarPelu.setVisibility(View.VISIBLE);

        //Obtiene las tarifas de la BD del peluquero en Firebase.
        firestore.collection("peluqueros").document(idPeluquero)
                .collection("peluqueria").document("tarifas").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {

                            if (task.getResult().exists()) {

                                Tarifas tarifas = task.getResult().toObject(Tarifas.class);

                                muestraTarifas(tarifas);

                                //Finalizada la carga, se vuelve a invisibilizar el círculo de carga.
                                circuloCargaTarPelu.setVisibility(View.INVISIBLE);

                            }else{
                                Toast.makeText(getApplicationContext(), getText(R.string.mensajeTarifasNoConfig),
                                        Toast.LENGTH_LONG).show();
                                finish();
                            }

                        }else{
                            Toast.makeText(getApplicationContext(), getText(R.string.mensajeErrorCargaTarifas),
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                circuloCargaTarPelu.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), getText(R.string.mensajeErrorCargaTarifas),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    /**
     * Método que se encarga de mostrar los elementos de la vista y precios según el contenido del
     * objeto obtenido.
     *
     * @param tarifas Objeto de tipo Tarifas con los datos obtenidos.
     */
    private void muestraTarifas(Tarifas tarifas){

        //Booleanos que controlan si existen tarifas establecidas y si se ha obtenido al menos un extra.
        Boolean existenTarifas=false, existenExtras=false;

        //Instancia del intent que se pasará a la activity de crear cita.
        intentCita = new Intent(this, CreaCitaActivity.class);

        //Precios de baño.
        if(tarifas.getBaseBanio()!=null){
            tvTaBanio.setVisibility(View.VISIBLE);
            tvTaBanioPrecio.setText(tarifas.getBaseBanio().toString()+MONEDA);
            intentCita.putExtra("banio", tarifas.getBaseBanio());
            existenTarifas=true;

            if(tarifas.getExtraBanio()!=null){
                tvTaBanioExtraPrecio.setText(tarifas.getExtraBanio().toString()+MONEDA);
                intentCita.putExtra("banioExtra", tarifas.getExtraBanio());
                existenExtras=true;
            }

        }

        //Precios de arreglo o corte parcial.
        if(tarifas.getBaseArreglo()!=null){
            tvTaArreglo.setVisibility(View.VISIBLE);
            tvTaArregloPrecio.setText(tarifas.getBaseArreglo().toString()+MONEDA);
            intentCita.putExtra("arreglo", tarifas.getBaseArreglo());
            existenTarifas=true;

            if(tarifas.getExtraArreglo()!=null){
                tvTaArregloExtraPrecio.setText(tarifas.getExtraArreglo().toString()+MONEDA);
                intentCita.putExtra("arregloExtra", tarifas.getExtraArreglo());
                existenExtras=true;
            }

        }

        //Precios de corte completo.
        if(tarifas.getBaseCorte()!=null){
            tvTaCompleto.setVisibility(View.VISIBLE);
            tvTaCompletoPrecio.setText(tarifas.getBaseCorte().toString()+MONEDA);
            intentCita.putExtra("corte", tarifas.getBaseCorte());
            existenTarifas=true;

            if(tarifas.getExtraCorte()!=null){
                tvTaCompletoExtraPrecio.setText(tarifas.getExtraCorte().toString()+MONEDA);
                intentCita.putExtra("corteExtra", tarifas.getExtraCorte());
                existenExtras=true;
            }

        }

        //Precios de deslanado.
        if(tarifas.getBaseDeslanado()!=null){
            tvTaDeslanado.setVisibility(View.VISIBLE);
            tvTaDeslanadoPrecio.setText(tarifas.getBaseDeslanado().toString()+MONEDA);
            intentCita.putExtra("deslanado", tarifas.getBaseDeslanado());
            existenTarifas=true;

            if(tarifas.getExtraDeslanado()!=null){
                tvTaDeslanadoExtraPrecio.setText(tarifas.getExtraDeslanado().toString()+MONEDA);
                intentCita.putExtra("deslanadoExtra", tarifas.getExtraDeslanado());
                existenExtras=true;
            }

        }

        //Precios de tinte.
        if(tarifas.getBaseTinte()!=null){
            tvTaTinte.setVisibility(View.VISIBLE);
            tvTaTintePrecio.setText(tarifas.getBaseTinte().toString()+MONEDA);
            intentCita.putExtra("tinte", tarifas.getBaseTinte());
            existenTarifas=true;

            if(tarifas.getExtraTinte()!=null){
                tvTaTinteExtraPrecio.setText(tarifas.getExtraTinte().toString()+MONEDA);
                intentCita.putExtra("tinteExtra", tarifas.getExtraTinte());
                existenExtras=true;
            }

        }

        //Precio de limpieza de oidos.
        if(tarifas.getPrecioOidos()!=null){
            tvTaOidos.setVisibility(View.VISIBLE);
            tvTaOidosPrecio.setText(tarifas.getPrecioOidos().toString()+MONEDA);
            intentCita.putExtra("oidos", tarifas.getPrecioOidos());
            existenTarifas=true;
        }

        //Precio de corte de uñas.
        if(tarifas.getPrecioUnias()!=null){
            tvTaUnias.setVisibility(View.VISIBLE);
            tvTaUniasPrecio.setText(tarifas.getPrecioUnias().toString()+MONEDA);
            intentCita.putExtra("unias", tarifas.getPrecioUnias());
            existenTarifas=true;
        }

        //Precio de limpieza de glándulas anales.
        if(tarifas.getPrecioAnales()!=null){
            tvTaAnales.setVisibility(View.VISIBLE);
            tvTaAnalesPrecio.setText(tarifas.getPrecioAnales().toString()+MONEDA);
            intentCita.putExtra("anales", tarifas.getPrecioAnales());
            existenTarifas=true;
        }

        //Si hay alguna tarifa establecida...
        if(existenTarifas) {

            //comprueba que haya algún extra establecido...
            if (existenExtras) {

                //Si es así, muestra un TextView con el peso de referencia para los extras y la cabecera de los mismos.
                tvPesoTarifas.setText(getString(R.string.edTaPeso1) + " "
                        + tarifas.getPesoExtra().toString() + " " + getString(R.string.edTaPeso2));
                intentCita.putExtra("pesoExtra", tarifas.getPesoExtra());
                tvTaExtra.setVisibility(View.VISIBLE);

            }

            //y activa el botón para pedir cita.
            bTarifasPedirCita.setEnabled(true);

        }

        //Activa el botón para volver atrás.
        bTarifasAtras.setEnabled(true);

    }

    /**
     * Método que instancia los elementos de la vista.
     */
    private void instanciasElementosVista(){

        tvTarifasPelu = findViewById(R.id.tvTarifasPelu);
        tvPesoTarifas = findViewById(R.id.tvPesoTarifas);
        tvTaExtra = findViewById(R.id.tvTaExtra);
        tvTaBanio = findViewById(R.id.tvTaBanio);
        tvTaBanioPrecio = findViewById(R.id.tvTaBanioPrecio);
        tvTaBanioExtraPrecio = findViewById(R.id.tvTaBanioPrecioExtra);
        tvTaArreglo = findViewById(R.id.tvTaArreglo);
        tvTaArregloPrecio = findViewById(R.id.tvTaArregloPrecio);
        tvTaArregloExtraPrecio = findViewById(R.id.tvTaArregloPrecioExtra);
        tvTaCompleto = findViewById(R.id.tvTaCompleto);
        tvTaCompletoPrecio = findViewById(R.id.tvTaCompletoPrecio);
        tvTaCompletoExtraPrecio = findViewById(R.id.tvTaCompletoPrecioExtra);
        tvTaDeslanado = findViewById(R.id.tvTaDeslanado);
        tvTaDeslanadoPrecio = findViewById(R.id.tvTaDeslanadoPrecio);
        tvTaDeslanadoExtraPrecio = findViewById(R.id.tvTaDeslanadoPrecioExtra);
        tvTaTinte = findViewById(R.id.tvTaTinte);
        tvTaTintePrecio = findViewById(R.id.tvTaTintePrecio);
        tvTaTinteExtraPrecio = findViewById(R.id.tvTaTintePrecioExtra);
        tvTaOidos = findViewById(R.id.tvTaOidos);
        tvTaOidosPrecio = findViewById(R.id.tvTaOidosPrecio);
        tvTaUnias = findViewById(R.id.tvTaUnias);
        tvTaUniasPrecio = findViewById(R.id.tvTaUniasPrecio);
        tvTaAnales = findViewById(R.id.tvTaAnales);
        tvTaAnalesPrecio = findViewById(R.id.tvTaAnalesPrecio);

        bTarifasAtras = findViewById(R.id.bTarifasAtras);
        bTarifasPedirCita = findViewById(R.id.bTarifasPedirCita);
        /*Inician desactivados los botones de la activity. No se podrán usar hasta
        que los datos se hayan cargado.*/
        bTarifasAtras.setEnabled(false);
        bTarifasPedirCita.setEnabled(false);

    }

    /**
     * Método que inicia la activity para pedir cita.
     *
     * @param view
     */
    protected void iniciaPedirCita(View view){

        //Desactiva los botones de la activity actual...
        bTarifasPedirCita.setEnabled(false);
        bTarifasAtras.setEnabled(false);

        //incluye la ID y nombre del peluquero en el intent...
        intentCita.putExtra("idPeluquero", idPeluquero);
        intentCita.putExtra("nombrePeluquero", nombrePeluquero);

        //e inicia la activity de creación de cita.
        startActivity(intentCita);

    }

    /**
     * Método para salir de la activity.
     *
     * @param view
     */
    protected void atras (View view){

        finish();

    }

}
