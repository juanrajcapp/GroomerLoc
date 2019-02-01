package com.juanrajc.groomerloc;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.juanrajc.groomerloc.clasesBD.Cita;
import com.juanrajc.groomerloc.clasesBD.Perro;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CreaCitaActivity extends AppCompatActivity implements CheckBox.OnCheckedChangeListener {

    //Constantes con los posibles resultados devueltos a la activity actual.
    private static final int REQUEST_PERRO=1;

    //Tipo de moneda usada en las tarifas.
    private final String MONEDA=" €";

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

    //Objeto del usuario actual y de la BD Firestore.
    private FirebaseUser usuario;
    private FirebaseFirestore firestore;

    //Datos necesarios para crear la cita.
    private String idPeluquero, idPerro;
    private Date fechaCreacion;
    private Perro perro;
    private Float pesoMascota, precioTotal;

    //Precios del pelquero.
    private Float banio, banioExtra, arreglo, arregloExtra, corte,
            corteExtra, deslanado, deslanadoExtra, tinte, tinteExtra,
            oidos, unias, anales, pesoExtra;

    //Archivo temporal que aloja una imagen.
    private File temp;

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

        //Instancia del usuario actual y de la base de datos Firestore.
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

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

    @Override
    protected void onResume() {
        super.onResume();

        //Al recibir el nombre del perro, se vuelven a activar algunos botones de la activity...
        bCreaCitaSelecMascota.setEnabled(true);
        bCreaCitaAtras.setEnabled(true);

        //y se comprueba si se debe activar el de confirmar.
        if(precioTotal!=null){
            bCreaCitaConfirmar.setEnabled(true);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Si el resultado devuelto a la activity es satisfactoria...
        if(resultCode==RESULT_OK) {

            //Comprueba mediante un switch el tipo de resultado devuelto.
            switch (requestCode) {

                case REQUEST_PERRO:

                    //Al recibir el ID de la mascota, se guarda en una variable de clase...
                    idPerro = data.getExtras().getString("idPerro");
                    //y se cargan sus datos.
                    cargaMascota(idPerro);

            }
        } else{
            Toast.makeText(this, getString(R.string.mensajeCreaCitaPerroNoRecibido),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        //Cada vez que se modifica el estado de un CheckBox, se comprueba si hay alguna mascota cargada...
        if(perro != null) {
            //si es así, se calcula el precio y se muestra.
            tvCreaCitaPrecio.setText(calculaPrecio());
        }

    }

    /**
     * Método que carga las tarifas del peluquero, pasadas por Intent, y muestra las opciones disponibles.
     */
    private void cargaOpciones(){

        //Precios de baño.
        if(getIntent().hasExtra("banio")){

            banio = getIntent().getFloatExtra("banio", 0f);
            cbCreaCitaBanio.setVisibility(View.VISIBLE);
            cbCreaCitaBanio.setOnCheckedChangeListener(this);

            if(getIntent().hasExtra("banioExtra")){
                banioExtra = getIntent().getFloatExtra("banioExtra", 0f);
            }

        }

        //Precios de arreglo o corte parcial.
        if(getIntent().hasExtra("arreglo")){

            arreglo = getIntent().getFloatExtra("arreglo", 0f);
            cbCreaCitaArreglo.setVisibility(View.VISIBLE);
            cbCreaCitaArreglo.setOnCheckedChangeListener(this);

            if(getIntent().hasExtra("arregloExtra")){
                arregloExtra = getIntent().getFloatExtra("arregloExtra", 0f);
            }

        }

        //Precios de corte completo.
        if(getIntent().hasExtra("corte")){

            corte = getIntent().getFloatExtra("corte", 0f);
            cbCreaCitaCorte.setVisibility(View.VISIBLE);
            cbCreaCitaCorte.setOnCheckedChangeListener(this);

            if(getIntent().hasExtra("corteExtra")){
                corteExtra = getIntent().getFloatExtra("corteExtra", 0f);
            }

        }

        //Precios de deslanado.
        if(getIntent().hasExtra("deslanado")){

            deslanado = getIntent().getFloatExtra("deslanado", 0f);
            cbCreaCitaDeslanado.setVisibility(View.VISIBLE);
            cbCreaCitaDeslanado.setOnCheckedChangeListener(this);

            if(getIntent().hasExtra("deslanadoExtra")){
                deslanadoExtra = getIntent().getFloatExtra("deslanadoExtra", 0f);
            }

        }

        //Precios de tinte.
        if(getIntent().hasExtra("tinte")){

            tinte = getIntent().getFloatExtra("tinte", 0f);
            cbCreaCitaTinte.setVisibility(View.VISIBLE);
            cbCreaCitaTinte.setOnCheckedChangeListener(this);

            if(getIntent().hasExtra("tinteExtra")){
                tinteExtra = getIntent().getFloatExtra("tinteExtra", 0f);
            }

        }

        //Precio de limpieza de oidos.
        if(getIntent().hasExtra("oidos")){

            oidos = getIntent().getFloatExtra("oidos", 0f);
            cbCreaCitaOidos.setVisibility(View.VISIBLE);
            cbCreaCitaOidos.setOnCheckedChangeListener(this);

        }

        //Precio de corte de uñas.
        if(getIntent().hasExtra("unias")){

            unias = getIntent().getFloatExtra("unias", 0f);
            cbCreaCitaUnias.setVisibility(View.VISIBLE);
            cbCreaCitaUnias.setOnCheckedChangeListener(this);

        }

        //Precio de limpieza de glándulas anales.
        if(getIntent().hasExtra("anales")){

            anales = getIntent().getFloatExtra("anales", 0f);
            cbCreaCitaAnales.setVisibility(View.VISIBLE);
            cbCreaCitaAnales.setOnCheckedChangeListener(this);

        }

        //Peso referencia para los extras.
        if(getIntent().hasExtra("pesoExtra")){
            pesoExtra = getIntent().getFloatExtra("pesoExtra", 0f);
        }

        //Finalizada la carga, se vuelve a invisibilizar el círculo de carga...
        circuloCargaCreaCita.setVisibility(View.INVISIBLE);

        //y se activan los botones de selección de mascota y atrás.
        bCreaCitaSelecMascota.setEnabled(true);
        bCreaCitaAtras.setEnabled(true);

    }

    /**
     * Método que carga los datos guardados de la mascota pasada por parámetro.
     *
     * @param idPerro Cadena con la ID de la mascota.
     */
    private void cargaMascota(final String idPerro){

        //Se visibiliza el círculo de carga.
        circuloCargaCreaCita.setVisibility(View.VISIBLE);

        //Consulta a Firebase Firestore los datos del perro seleccionado.
        firestore.collection("clientes").document(usuario.getUid())
                .collection("perros").document(idPerro)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                //Si la consulta ha resultado exitosa...
                if(task.isSuccessful()){

                    //guarda el resultado en un DocumentSnapshot.
                    DocumentSnapshot doc = task.getResult();

                    //Si existe un resultado...
                    if(doc.exists()){

                        //se obtiene y guarda el objeto con los datos de la mascota...
                        perro = doc.toObject(Perro.class);
                        //se guarda el peso en una variable de clase...
                        pesoMascota = perro.getPeso();
                        //se muestra el nombre en la interfaz de la activity...
                        tvCreaCitaMascota.setText(perro.getNombre());
                        //y por último, se muestra el precio calculado en la interfaz de la activity.
                        tvCreaCitaPrecio.setText(calculaPrecio());

                    //Si no...
                    }else{
                        //muestra un mensaje...
                        Toast.makeText(getApplicationContext(), getText(R.string.mensajePerroNoExiste),
                                Toast.LENGTH_SHORT).show();
                    }
                //Si no...
                }else{
                    //muestra un mensaje...
                    Toast.makeText(getApplicationContext(), getText(R.string.mensajePerroNoExiste),
                            Toast.LENGTH_SHORT).show();
                }

                //Finalizada la carga, se vuelve a invisibilizar el círculo de carga...
                circuloCargaCreaCita.setVisibility(View.INVISIBLE);

                //y se vuelve a activar la posibilidad de pulsación de los botones.
                bCreaCitaSelecMascota.setEnabled(true);
                bCreaCitaAtras.setEnabled(true);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getText(R.string.mensajePerroNoExiste),
                        Toast.LENGTH_SHORT).show();
                circuloCargaCreaCita.setVisibility(View.INVISIBLE);
                bCreaCitaSelecMascota.setEnabled(true);
                bCreaCitaAtras.setEnabled(true);
            }
        });

    }

    /**
     * Método que se encarga de calcular el precio total conjunto de los servicios seleccionados.
     *
     * @return Cadena con el precio calculado.
     */
    private String calculaPrecio(){

        Float precio=0f;
        boolean algunaSeleccion=false;

        //Precios de baño.
        if(cbCreaCitaBanio.isChecked()){
            precio+=banio;
            algunaSeleccion=true;

            if(banioExtra!=null){
                precio+=calculaExtra(banioExtra);
            }

        }

        //Precios de arreglo o corte parcial.
        if(cbCreaCitaArreglo.isChecked()){
            precio+=arreglo;
            algunaSeleccion=true;

            if(arregloExtra!=null){
                precio+=calculaExtra(arregloExtra);
            }

        }

        //Precios de corte completo.
        if(cbCreaCitaCorte.isChecked()){
            precio+=corte;
            algunaSeleccion=true;

            if(corteExtra!=null){
                precio+=calculaExtra(corteExtra);
            }

        }

        //Precios de deslanado.
        if(cbCreaCitaDeslanado.isChecked()){
            precio+=deslanado;
            algunaSeleccion=true;

            if(deslanadoExtra!=null){
                precio+=calculaExtra(deslanadoExtra);
            }

        }

        //Precios de tinte.
        if(cbCreaCitaTinte.isChecked()){
            precio+=tinte;
            algunaSeleccion=true;

            if(tinteExtra!=null){
                precio+=calculaExtra(tinteExtra);
            }

        }

        //Precio de limpieza de oidos.
        if(cbCreaCitaOidos.isChecked()){
            precio+=oidos;
            algunaSeleccion=true;
        }

        //Precio de corte de uñas.
        if(cbCreaCitaUnias.isChecked()){
            precio+=unias;
            algunaSeleccion=true;
        }

        //Precio de limpieza de glándulas anales.
        if(cbCreaCitaAnales.isChecked()){
            precio+=anales;
            algunaSeleccion=true;
        }

        //Comprueba que al menos un servicio ha sido seleccionado.
        if(algunaSeleccion){

            precioTotal = precio;

            bCreaCitaConfirmar.setEnabled(true);

            return precio.toString()+MONEDA;

        }else{

            precioTotal = null;

            bCreaCitaConfirmar.setEnabled(false);

            return "";

        }

    }

    /**
     * Método que se encarga de calcular el suplemento de precio por peso.
     *
     * @param precioExtra Float con el suplemento por peso.
     *
     * @return Float con el precio del suplemento ya calculado respecto al peso de la mascota.
     */
    private Float calculaExtra(Float precioExtra){

        int vecesExtra = (int) (pesoMascota/pesoExtra);

        return precioExtra*vecesExtra;

    }

    /**
     * Método que crea la descripción del servicio a prestar en la cita.
     *
     * @return Cadena con la descripción del servicio.
     */
    private String creaServicio(){

        StringBuffer sb=new StringBuffer();

        //Baño.
        if(cbCreaCitaBanio.isChecked()){
            sb.append("| BAÑO |");
        }

        //Arreglo o corte parcial.
        if(cbCreaCitaArreglo.isChecked()){
            sb.append("| ARREGLO |");
        }

        //Corte completo.
        if(cbCreaCitaCorte.isChecked()){
            sb.append("| CORTE |");
        }

        //Deslanado.
        if(cbCreaCitaDeslanado.isChecked()){
            sb.append("| DESLANADO |");
        }

        //Tinte.
        if(cbCreaCitaTinte.isChecked()){
            sb.append("| TINTE |");
        }

        //Limpieza de oidos.
        if(cbCreaCitaOidos.isChecked()){
            sb.append("| LIMP. OIDOS |");
        }

        //Corte de uñas.
        if(cbCreaCitaUnias.isChecked()){
            sb.append("| CORT. UÑAS |");
        }

        //Limpieza de glándulas anales.
        if(cbCreaCitaAnales.isChecked()) {
            sb.append("| LIMP. GLÁNDULAS |");
        }

        return sb.toString();

    }

    /**
     * Método que guarda la foto actual del perro en la ubicación de la cita.
     *
     * @param idCita Cadena con la ID de la cita en Firestore.
     */
    private void guardaFotoPerro(final String idCita){

        try {

            // Crea un nombre para el archivo con la imagen.
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            //Crea el archivo temporal.
            temp = File.createTempFile(
                    imageFileName,  /* prefijo */
                    ".jpg",         /* sufijo */
                    storageDir      /* directorio */
            );

            //Descarga la imagen del perro seleccionado desde Firebase Storage y la guarda en el archivo temporal.
            FirebaseStorage.getInstance().getReference().child("fotos/"+usuario.getUid()
                    +"/perros/"+idPerro+" "+perro.getFechaFoto()+".jpg").getFile(temp)
                    .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                    if(task.isSuccessful() && task.isComplete()){

                        //Cuando termina de descargarse, se sube a la ubicación de la cita.
                        FirebaseStorage.getInstance().getReference("citas/" + idCita
                                + "/perros/" + perro.getNombre() + ".jpg")
                                .putFile(Uri.parse("file:"+temp.getAbsolutePath()));

                    }
                }
            });

        } catch (IOException e) {
        }

    }

    /**
     * Método que crea la cita.
     *
     * @param view
     */
    protected void creaCita(View view){

        //Se desactivan los botones de la activity para evitar varias pulsaciones simultáneas.
        bCreaCitaSelecMascota.setEnabled(false);
        bCreaCitaAtras.setEnabled(false);
        bCreaCitaConfirmar.setEnabled(false);

        //Crea la cita en Firestore.
        firestore.collection("citas").add(new Cita(idPeluquero, usuario.getUid(),
                creaServicio(), precioTotal, fechaCreacion, null, perro))
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        //Si el perro seleccionado tiene foto, se guarda en la ubicación de la cita.
                        if(perro.getFechaFoto()!=null){
                            guardaFotoPerro(documentReference.getId());
                        }

                        Toast.makeText(getApplicationContext(), getString(R.string.mensajeCreaCitaCreadaExito),
                                Toast.LENGTH_LONG).show();

                        /*startActivity(new Intent(getApplicationContext(), ChatCitaActivity.class)
                                .putExtra("refCita", documentReference.getId()));*/

                        finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.mensajeCreaCitaCreadaError),
                        Toast.LENGTH_SHORT).show();
                bCreaCitaSelecMascota.setEnabled(true);
                bCreaCitaAtras.setEnabled(true);
                bCreaCitaConfirmar.setEnabled(true);
            }
        });

    }

    /**
     * Método que inicia la activity de selección de mascota.
     *
     * @param view
     */
    protected void seleccionaMascota(View view){

        //Se desactivan los botones de la activity para evitar varias pulsaciones simultáneas.
        bCreaCitaSelecMascota.setEnabled(false);
        bCreaCitaAtras.setEnabled(false);
        bCreaCitaConfirmar.setEnabled(false);

        //Inicia la activity que devolverá la mascota seleccionada por el cliente.
        startActivityForResult(new Intent(this, PerrosCitaActivity.class), REQUEST_PERRO);

    }

    /**
     * Método que controla el retroceso a la activity anterior.
     *
     * @param view
     */
    protected void atras (View view){

        //Se desactivan los botones de la activity para evitar varias pulsaciones simultáneas.
        bCreaCitaSelecMascota.setEnabled(false);
        bCreaCitaAtras.setEnabled(false);
        bCreaCitaConfirmar.setEnabled(false);

        //Finaliza la activity.
        finish();

    }

}
