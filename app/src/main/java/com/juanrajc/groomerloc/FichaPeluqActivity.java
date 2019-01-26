package com.juanrajc.groomerloc;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.juanrajc.groomerloc.clasesBD.Peluquero;

import java.io.IOException;
import java.util.List;

public class FichaPeluqActivity extends AppCompatActivity {

    //Objeto de la BD Firestore.
    private FirebaseFirestore firestore;

    //Objetos de los elementos visibles de la activity.
    private TextView tvNombrePelu, tvTelPelu, tvDirPelu, tvDir2Pelu, tvTitDir2Pelu;
    private ImageView ivImagenPelu;
    private Button bTarifasPelu, bAtrasFichaPelu;

    //Objeto del círculo de carga.
    private ProgressBar circuloCargaPelu;

    //Geocoder para la traducción de coordenadas en direciones y viceversa.
    private Geocoder gc;

    private String idPeluquero, nombrePeluquero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ficha_peluq);

        //Instancia de la base de datos de Firebase.
        firestore=FirebaseFirestore.getInstance();

        //Instancia del círculo de carga.
        circuloCargaPelu = (ProgressBar) findViewById(R.id.circuloCargaPelu);

        //Instancias de los elementos visibles de la activity.
        tvNombrePelu = findViewById(R.id.tvNombrePelu);
        tvTelPelu = findViewById(R.id.tvTelPelu);
        tvDirPelu = findViewById(R.id.tvDirPelu);
        tvDir2Pelu = findViewById(R.id.tvDir2Pelu);
        ivImagenPelu = findViewById(R.id.ivImagenPelu);

        tvTitDir2Pelu = findViewById(R.id.tvTitDir2Pelu);
        /*Inicia invisible el título de la información adicional, solo se mostrará
        si el peluquero ha introducido algún dato en dicho campo.*/
        tvTitDir2Pelu.setVisibility(View.INVISIBLE);

        bTarifasPelu = findViewById(R.id.bTarifasPelu);
        bAtrasFichaPelu = findViewById(R.id.bAtrasFichaPelu);
        /*Inician desactivados los botones de la activity. No se podrán usar hasta
        que los datos se hayan cargado.*/
        bTarifasPelu.setEnabled(false);
        bAtrasFichaPelu.setEnabled(false);

        //Instancia del Geocoder.
        gc=new Geocoder(this);

        //Recoge y guarda el ID del peluquero...
        idPeluquero = getIntent().getStringExtra("idPeluquero");

        //y se pasa al método que carga los datos.
        cargaInfoPelu(idPeluquero);

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Al volver a la activity, se vuelven a activar los botones de la misma.
        bTarifasPelu.setEnabled(true);
        bAtrasFichaPelu.setEnabled(true);

    }

    /**
     * Método que se encarga de cargar los datos del peluquero seleccionado desde Firestore.
     *
     * @param idPeluquero Cadena con la ID del peluquero.
     */
    private void cargaInfoPelu(String idPeluquero){

        //Se visibiliza el círculo de carga.
        circuloCargaPelu.setVisibility(View.VISIBLE);

        //Carga los datos del peluquero indicando su ID, que es el también el nombre de su documento en Firestore...
        firestore.collection("peluqueros").document(idPeluquero).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    //si existe...
                    if(task.getResult().exists()){

                        Peluquero peluquero = task.getResult().toObject(Peluquero.class);

                        //Guarda el nombre en una variable...
                        nombrePeluquero = peluquero.getNombre();

                        //se muestran los datos...
                        tvNombrePelu.setText(peluquero.getNombre());
                        tvTelPelu.setText(String.valueOf(peluquero.getTelefono()));
                        tvDirPelu.setText(obtieneDireccion(new LatLng(peluquero.getLoc().getLatitude(),
                                peluquero.getLoc().getLongitude())));

                        //Comprueba que el campo de informacion adicional contiene datos...
                        if(peluquero.getLocExtra().length()>0) {

                            //si es así, se muestra su título y el contenido del mismo.
                            tvTitDir2Pelu.setVisibility(View.VISIBLE);
                            tvDir2Pelu.setText(peluquero.getLocExtra());
                        }

                        //y se activan los botones de la activity.
                        bTarifasPelu.setEnabled(true);
                        bAtrasFichaPelu.setEnabled(true);


                    //Si no...
                    }else{
                        Toast.makeText(getApplicationContext(), getString(R.string.mensajeErrorCargaDatosPelu),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.mensajeErrorCargaDatosPelu),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }

                //Finalizada la carga, se vuelve a invisibilizar el círculo de carga.
                circuloCargaPelu.setVisibility(View.INVISIBLE);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                circuloCargaPelu.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), getString(R.string.mensajeErrorCargaDatosPelu),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    /**
     * Método que se encarga de traducir las coordenadas en una dirección postal.
     *
     * @param coordenadas LatLng con las coordenadas que se quieren traducir.
     *
     * @return Cadena con la dirección postal obtenida mediante las coordenadas.
     */
    private String obtieneDireccion(LatLng coordenadas) {

        try {

            //Obtiene la dirección mediante las coordenadas obtenidas por parámetro.
            List<Address> direcciones = gc.getFromLocation(coordenadas.latitude, coordenadas.longitude, 1);

            //Comprueba que se ha guardado al menos una dirección.
            if(direcciones.size()>0){
                //Si es así, devuelve el primero obtenido.
                return muestraDireccion(direcciones.get(0));
            }else{
                //Si no, devuelve una cadena vacía.
                return "";
            }

        }catch (IOException ioe){
            return "";
        }

    }

    /**
     * Método que se encarga de generar una dirección postal más comprensible.
     *
     * @param direccion Objeto Address con la dirección postal.
     * @return Devuelve una cadena con la dirección postal simplificada.
     */
    protected String muestraDireccion(Address direccion) {

        String numero=direccion.getSubThoroughfare(),
                calle=direccion.getThoroughfare(),
                barrio=direccion.getSubLocality(),
                localidad=direccion.getLocality(),
                provincia=direccion.getSubAdminArea(),
                caOEstado=direccion.getAdminArea(),
                pais=direccion.getCountryName();

        StringBuffer sb=new StringBuffer();

        if(numero!=null){
            sb.append(numero+", ");
        }
        if(calle!=null){
            sb.append(calle+", ");
        }
        if(barrio!=null){
            sb.append(barrio+", ");
        }
        if(localidad!=null){
            sb.append(localidad+", ");
        }
        if(provincia!=null && !provincia.equalsIgnoreCase(localidad)){
            sb.append(provincia+", ");
        }
        if(caOEstado!=null){
            sb.append(caOEstado+", ");
        }
        if(pais!=null){
            sb.append(pais);
        }

        return sb.toString();

    }

    /**
     * Método que inicia la activity que muestra las tarifas del peluquero.
     *
     * @param view
     */
    protected void iniciaVistaTarifas(View view){

        //Desactiva los botones de la activity actual...
        bTarifasPelu.setEnabled(false);
        bAtrasFichaPelu.setEnabled(false);

        //E inicia la que muestra las tarifas, pasándole la ID y nombre del peluquero.
        startActivity(new Intent(this, TarifasPeluActivity.class)
                .putExtra("idPeluquero", idPeluquero)
                .putExtra("nombrePeluquero", nombrePeluquero));

    }

    /**
     * Método que controla el retroceso a la activity anterior.
     *
     * @param view
     */
    protected void atras (View view){

        ////Se desactivan los botones de la activity para evitar varias pulsaciones simultáneas.
        bTarifasPelu.setEnabled(false);
        bAtrasFichaPelu.setEnabled(false);

        //Finaliza la activity.
        finish();

    }

}
