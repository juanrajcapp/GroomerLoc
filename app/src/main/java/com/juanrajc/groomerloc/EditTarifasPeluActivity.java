package com.juanrajc.groomerloc;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.juanrajc.groomerloc.clasesBD.Tarifas;

public class EditTarifasPeluActivity extends AppCompatActivity implements TextWatcher{

    //Objetos de los campos editables de la vista.
    private EditText etEdTaBanio, etEdTaBanioExtra, etEdTaArreglo, etEdTaArregloExtra,
            etEdTaCompleto, etEdTaCompletoExtra, etEdTaDeslanado, etEdTaDeslanadoExtra,
            etEdTaTinte, etEdTaTinteExtra, etEdTaPeso, etEdTaOidos, etEdTaUnias, etEdTaAnales;

    //Objetos de los botones de la vista.
    private Button bEditTarifasAtras, bEditTarifasGuardar;

    //Objetos de Firebase (Autenticación y BD Firestore).
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    //Objeto del círculo de carga.
    private ProgressBar circuloCargaEdTarPelu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tarifas_pelu);

        //Instancia del círculo de carga.
        circuloCargaEdTarPelu = (ProgressBar) findViewById(R.id.circuloCargaEdTarPelu);

        //Instancia de los campos editables de la vista
        etEdTaBanio = findViewById(R.id.etEdTaBanio);
        etEdTaBanioExtra = findViewById(R.id.etEdTaBanioExtra);
        etEdTaArreglo = findViewById(R.id.etEdTaArreglo);
        etEdTaArregloExtra = findViewById(R.id.etEdTaArregloExtra);
        etEdTaCompleto = findViewById(R.id.etEdTaCompleto);
        etEdTaCompletoExtra = findViewById(R.id.etEdTaCompletoExtra);
        etEdTaDeslanado = findViewById(R.id.etEdTaDeslanado);
        etEdTaDeslanadoExtra = findViewById(R.id.etEdTaDeslanadoExtra);
        etEdTaTinte = findViewById(R.id.etEdTaTinte);
        etEdTaTinteExtra = findViewById(R.id.etEdTaTinteExtra);
        etEdTaPeso = findViewById(R.id.etEdTaPeso);
        etEdTaOidos = findViewById(R.id.etEdTaOidos);
        etEdTaUnias = findViewById(R.id.etEdTaUnias);
        etEdTaAnales = findViewById(R.id.etEdTaAnales);

        //Instancia de los botones de la vista.
        bEditTarifasAtras = findViewById(R.id.bEditTarifasAtras);
        bEditTarifasGuardar = findViewById(R.id.bEditTarifasGuardar);
        /*Inician 'no clicables' los botones de la activity. No se podrán cliquear hasta
        que los datos se hayan cargado.*/
        bEditTarifasAtras.setClickable(false);
        bEditTarifasGuardar.setClickable(false);

        //Al iniciar la activity, se deshabilitan algunos campos editables de la vista.
        etEdTaBanioExtra.setEnabled(false);
        etEdTaArregloExtra.setEnabled(false);
        etEdTaCompletoExtra.setEnabled(false);
        etEdTaDeslanadoExtra.setEnabled(false);
        etEdTaTinteExtra.setEnabled(false);
        etEdTaPeso.setEnabled(false);

        //Listeners de los campos editables que lo requieren.
        etEdTaBanio.addTextChangedListener(this);
        etEdTaBanioExtra.addTextChangedListener(this);
        etEdTaArreglo.addTextChangedListener(this);
        etEdTaArregloExtra.addTextChangedListener(this);
        etEdTaCompleto.addTextChangedListener(this);
        etEdTaCompletoExtra.addTextChangedListener(this);
        etEdTaDeslanado.addTextChangedListener(this);
        etEdTaDeslanadoExtra.addTextChangedListener(this);
        etEdTaTinte.addTextChangedListener(this);
        etEdTaTinteExtra.addTextChangedListener(this);

        //Instancias de la autenticación y la base de datos de Firebase.
        auth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();

        cargaTarifas();

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {

        Boolean contieneNumeros = editable.toString().length()>0;

        if(editable.hashCode() == etEdTaBanio.getText().hashCode()){

            if(contieneNumeros){
                etEdTaBanioExtra.setEnabled(true);
            }else{
                etEdTaBanioExtra.setText("");
                etEdTaBanioExtra.setEnabled(false);
            }

        }else if(editable.hashCode() == etEdTaArreglo.getText().hashCode()){

            if(contieneNumeros){
                etEdTaArregloExtra.setEnabled(true);
            }else{
                etEdTaArregloExtra.setText("");
                etEdTaArregloExtra.setEnabled(false);
            }

        }else if(editable.hashCode() == etEdTaCompleto.getText().hashCode()){

            if(contieneNumeros){
                etEdTaCompletoExtra.setEnabled(true);
            }else{
                etEdTaCompletoExtra.setText("");
                etEdTaCompletoExtra.setEnabled(false);
            }

        }else if(editable.hashCode() == etEdTaDeslanado.getText().hashCode()){

            if(contieneNumeros){
                etEdTaDeslanadoExtra.setEnabled(true);
            }else{
                etEdTaDeslanadoExtra.setText("");
                etEdTaDeslanadoExtra.setEnabled(false);
            }

        }else if(editable.hashCode() == etEdTaTinte.getText().hashCode()){

            if(contieneNumeros){
                etEdTaTinteExtra.setEnabled(true);
            }else{
                etEdTaTinteExtra.setText("");
                etEdTaTinteExtra.setEnabled(false);
            }

        }

        compruebaExtras();

    }

    /**
     * Método que guarda los datos de las tarifas introducidos por el peluquero en Firestore.
     *
     * @param view
     */
    public void guardaTarifas (View view){

        //Comprueba que, si se introduce un precio de extra, se haya introducido también el rango de peso.
        if((etEdTaBanioExtra.getText().toString().length()>0 || etEdTaArregloExtra.getText().toString().length()>0
                || etEdTaCompletoExtra.getText().toString().length()>0 || etEdTaDeslanadoExtra.getText().toString().length()>0
                || etEdTaTinteExtra.getText().toString().length()>0) && etEdTaPeso.getText().toString().length()<1){

            //Si no, muestra un mensaje y mueve el focus al campo del peso.
            Toast.makeText(getApplicationContext(), getString(R.string.mensajeAvisoPesoExtra),
                    Toast.LENGTH_SHORT).show();
            etEdTaPeso.requestFocus();

        }else {

            //Se desactivan los botones de la activity para evitar varias pulsaciones simultáneas.
            bEditTarifasGuardar.setClickable(false);
            bEditTarifasAtras.setClickable(false);

            /*
            Guarda en la colección "peluquería" -> documento "tarifas" de la BD del peluquero
            actualmente autenticado los datos introducidos por el mismo, mediante un POJO.
            */
            firestore.collection("peluqueros").document(auth.getCurrentUser().getUid())
                    .collection("peluqueria").document("tarifas")
                    .set(new Tarifas(manejaValorCampo(etEdTaBanio), manejaValorCampo(etEdTaBanioExtra),
                            manejaValorCampo(etEdTaArreglo), manejaValorCampo(etEdTaArregloExtra),
                            manejaValorCampo(etEdTaCompleto), manejaValorCampo(etEdTaCompletoExtra),
                            manejaValorCampo(etEdTaDeslanado), manejaValorCampo(etEdTaDeslanadoExtra),
                            manejaValorCampo(etEdTaTinte), manejaValorCampo(etEdTaTinteExtra),
                            manejaValorCampo(etEdTaPeso), manejaValorCampo(etEdTaOidos),
                            manejaValorCampo(etEdTaUnias), manejaValorCampo(etEdTaAnales)))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), getString(R.string.mensajeTarGuardadas),
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(getApplicationContext(), getString(R.string.mensajeTarNoGuardadas),
                            Toast.LENGTH_SHORT).show();

                    //Si falla el guardado, se vuelven a activar los botones de la activity.
                    bEditTarifasGuardar.setClickable(true);
                    bEditTarifasAtras.setClickable(true);

                }
            });

        }

    }

    /**
     * Método que controla el retroceso a la activity anterior.
     *
     * @param view
     */
    public void atras (View view){

        ////Se desactivan los botones de la activity para evitar varias pulsaciones simultáneas.
        bEditTarifasGuardar.setClickable(false);
        bEditTarifasAtras.setClickable(false);

        //Finaliza la activity.
        finish();

    }

    /**
     * Método que se encarga de parsear y devolver un valor correcto desde un EditText de la vista.
     *
     * @param entrada Objeto EditText que contiene el valor a manejar.
     * @return Objeto Float con el valor obtenido.
     */
    private Float manejaValorCampo(EditText entrada){

        //Obtiene la cadena contenida en el EditText.
        String valor = entrada.getText().toString();

        //Si el String contiene datos...
        if(valor.length()>0){

            //parsea y devuelve un objeto Float con el valor.
            return Float.parseFloat(valor);

        //Si no...
        }else{

            //devuelve nulo.
            return null;

        }

    }

    /**
     * Método que se encarga de comprobar si hay algún campo de "extras" con un valor introducido.
     */
    private void compruebaExtras(){

        //Si algún EditText de "extras" contiene algún valor...
        if(etEdTaBanioExtra.getText().toString().length()>0
                || etEdTaArregloExtra.getText().toString().length()>0
                || etEdTaCompletoExtra.getText().toString().length()>0
                || etEdTaDeslanadoExtra.getText().toString().length()>0
                || etEdTaTinteExtra.getText().toString().length()>0){

            //se activa el campo de peso.
            etEdTaPeso.setEnabled(true);

        //Si no...
        } else {

            //Se borra el contenido del campo de peso y se desactiva (no permite introducir valores en él).
            etEdTaPeso.setText("");
            etEdTaPeso.setEnabled(false);

        }

    }

    /**
     * Método que se encarga de obtener desde la BD del peluquero en Firebase las tarifas
     * guardadas (si existen).
     */
    private void cargaTarifas(){

        //Se visibiliza el círculo de carga.
        circuloCargaEdTarPelu.setVisibility(View.VISIBLE);

        //Obtiene las tarifas de la BD del peluquero en Firebase.
        firestore.collection("peluqueros").document(auth.getCurrentUser().getUid())
                .collection("peluqueria").document("tarifas").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {

                            if (task.getResult().exists()) {

                                Tarifas tarifas = task.getResult().toObject(Tarifas.class);

                                rellenaCampos(tarifas);

                            }

                            //Existan o no existan datos, se activa los botones de la activity.
                            bEditTarifasAtras.setClickable(true);
                            bEditTarifasGuardar.setClickable(true);

                        }else{
                            Toast.makeText(getApplicationContext(), getText(R.string.mensajeErrorCargaTarifas),
                                    Toast.LENGTH_SHORT).show();
                            bEditTarifasAtras.setClickable(true);
                        }

                        //Finalizada la carga, se vuelve a invisibilizar el círculo de carga.
                        circuloCargaEdTarPelu.setVisibility(View.INVISIBLE);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        circuloCargaEdTarPelu.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), getText(R.string.mensajeErrorCargaTarifas),
                                Toast.LENGTH_SHORT).show();
                        bEditTarifasAtras.setClickable(true);
                    }
                });

    }

    /**
     * Método que rellena los campos de las tarifas con los datos obtenidos de la BD
     * del peluquero en Firebase.
     *
     * @param tarifas Objeto de tipo Tarifas con los datos obtenidos de la BD.
     */
    private void rellenaCampos(Tarifas tarifas){

        //Precios de baño.
        if(tarifas.getBaseBanio()!=null){
            etEdTaBanio.setText(tarifas.getBaseBanio().toString());

            if(tarifas.getExtraBanio()!=null){
                etEdTaBanioExtra.setText(tarifas.getExtraBanio().toString());
            }

        }

        //Precios de arreglo o corte parcial.
        if(tarifas.getBaseArreglo()!=null){
            etEdTaArreglo.setText(tarifas.getBaseArreglo().toString());

            if(tarifas.getExtraArreglo()!=null){
                etEdTaArregloExtra.setText(tarifas.getExtraArreglo().toString());
            }

        }

        //Precios de corte completo.
        if(tarifas.getBaseCorte()!=null){
            etEdTaCompleto.setText(tarifas.getBaseCorte().toString());

            if(tarifas.getExtraCorte()!=null){
                etEdTaCompletoExtra.setText(tarifas.getExtraCorte().toString());
            }

        }

        //Precios de deslanado.
        if(tarifas.getBaseDeslanado()!=null){
            etEdTaDeslanado.setText(tarifas.getBaseDeslanado().toString());

            if(tarifas.getExtraDeslanado()!=null){
                etEdTaDeslanadoExtra.setText(tarifas.getExtraDeslanado().toString());
            }
        }

        //Precios de tinte.
        if(tarifas.getBaseTinte()!=null){
            etEdTaTinte.setText(tarifas.getBaseTinte().toString());

            if(tarifas.getExtraTinte()!=null){
                etEdTaTinteExtra.setText(tarifas.getExtraTinte().toString());
            }

        }

        //Peso de los extras.
        if(tarifas.getPesoExtra()!=null){
            etEdTaPeso.setText(tarifas.getPesoExtra().toString());
        }

        //Precio de limpieza de oidos.
        if(tarifas.getPrecioOidos()!=null){
            etEdTaOidos.setText(tarifas.getPrecioOidos().toString());
        }

        //Precio de corte de uñas.
        if(tarifas.getPrecioUnias()!=null){
            etEdTaUnias.setText(tarifas.getPrecioUnias().toString());
        }

        //Precio de limpieza de glándulas anales.
        if(tarifas.getPrecioAnales()!=null){
            etEdTaAnales.setText(tarifas.getPrecioAnales().toString());
        }

    }

}
