package com.juanrajc.groomerloc;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.juanrajc.groomerloc.adaptadores.AdaptadorCitasPelu;
import com.juanrajc.groomerloc.clasesBD.Cita;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CitasConfPeluActivity extends AppCompatActivity {

    //Objeto del adaptador que muestra las citas concertadas.
    private RecyclerView rvCitasConfPelu;

    //Objetos de Firebase (Autenticación y BD Firestore).
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    //Objetos de los botones para recargar las citas y retroceder.
    private Button botonRecargarCitasConfPelu, botonAtrasCitasConfPelu;

    //Objeto del círculo de carga.
    private ProgressBar circuloCargaCitasConfPelu;

    //Objeto del EditText que aparece cuando no hay citas.
    private TextView tvNoCitasConfPelu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citas_conf_pelu);

        //Instancias de la autenticación y la base de datos de Firebase.
        auth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();

        //Instancias de los botones para recargar citas y retroceder.
        botonRecargarCitasConfPelu = (Button) findViewById(R.id.botonRecargarCitasConfPelu);
        botonAtrasCitasConfPelu = (Button) findViewById(R.id.botonAtrasCitasConfPelu);

        //Instancia del círculo de carga y mensaje de no existencia de citas.
        circuloCargaCitasConfPelu = (ProgressBar) findViewById(R.id.circuloCargaCitasConfPelu);
        tvNoCitasConfPelu = (TextView) findViewById(R.id.tvNoCitasConfPelu);

        //Instancia del RecyclerView de citas.
        rvCitasConfPelu = (RecyclerView) findViewById(R.id.rvCitasConfPelu);

        //Fija el tamaño del rv, que mejorará el rendimento.
        rvCitasConfPelu.setHasFixedSize(true);

        //Administrador para el LinearLayout.
        rvCitasConfPelu.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Se oculta el mensaje de la no existencia de citas...
        tvNoCitasConfPelu.setVisibility(View.INVISIBLE);

        //y se obtienen las citas.
        obtieneCitas();

    }

    /**
     * Método que obtiene las citas solicitadas con fecha establecida.
     */
    private void obtieneCitas(){

        //Se desactivan los botones de la activity para evitar dobles pulsaciones.
        botonRecargarCitasConfPelu.setEnabled(false);
        botonAtrasCitasConfPelu.setEnabled(false);

        //Se visibiliza el círculo de carga.
        circuloCargaCitasConfPelu.setVisibility(View.VISIBLE);

        //Obtiene las citas credas por el peluquero actual en orden de fecha de confirmación desde Firestore.
        firestore.collection("citas").whereEqualTo("idPeluquero", auth.getCurrentUser().getUid())
                .orderBy("fechaConfirmacion")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                //Si se obtienen resultados satisfactoriamente...
                if(task.isSuccessful()){

                    //comprueba que esos resultados contienen datos.
                    if(task.getResult().isEmpty()){
                        rvCitasConfPelu.setAdapter(new AdaptadorCitasPelu(null, null));
                        tvNoCitasConfPelu.setVisibility(View.VISIBLE);
                    }else {

                        /*
                        Si contienen datos, se crean dos List, una con las IDs y otra con
                        los objetos de las citas encontrados...
                        */
                        List<String> listaIdsCitas = new ArrayList<String>();
                        List<Cita> listaObjCitas = new ArrayList<Cita>();

                        //y se introducen dichos datos en dichos List...
                        for (QueryDocumentSnapshot doc : task.getResult()) {

                            //comprobando antes la vigencia de cada cita.
                            if(compruebaVigenciaCita(doc.toObject(Cita.class))) {

                                //Sólo se van a mostrar los que tengan establecida la fecha de confirmación.
                                if (doc.toObject(Cita.class).getFechaConfirmacion() != null) {

                                    listaIdsCitas.add(doc.getId());
                                    listaObjCitas.add(doc.toObject(Cita.class));

                                }

                            }else{

                                borraCita(doc.getId(), doc.toObject(Cita.class));

                            }

                        }

                        /*
                        Si no se ha añadido ninguna cita al list, se muestra un mensaje en
                        la vista de la activity.
                        */
                        if(listaIdsCitas.isEmpty()) {
                            tvNoCitasConfPelu.setVisibility(View.VISIBLE);
                        }

                        //Crea un nuevo adaptador con las citas obtenidas.
                        rvCitasConfPelu.setAdapter(new AdaptadorCitasPelu(listaIdsCitas, listaObjCitas));

                    }

                //Si ha habido algún problema al obtener resultados, se muestra un toast avisándolo.
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.mensajeNoResultCitas),
                            Toast.LENGTH_SHORT).show();
                }

                //Finalizada la carga, se vuelve a invisibilizar el círculo de carga y a activar los botones.
                circuloCargaCitasConfPelu.setVisibility(View.INVISIBLE);
                botonRecargarCitasConfPelu.setEnabled(true);
                botonAtrasCitasConfPelu.setEnabled(true);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                circuloCargaCitasConfPelu.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), getString(R.string.mensajeNoResultCitas),
                        Toast.LENGTH_SHORT).show();
                botonRecargarCitasConfPelu.setEnabled(true);
                botonAtrasCitasConfPelu.setEnabled(true);
            }
        });

    }

    /**
     * Método que comprueba si la cita es válida en el tiempo (que ha sido confirmada en el espacio
     * de tiempo de una semana desde su creación, y en el caso de que ya haya sido confirmada, que
     * la fecha de confirmación no haya expirado en un periodo de más de 6 horas).
     *
     * @param cita Objeto de la cita, el cual contiene sus datos.
     *
     * @return Booleano true si la cita está vigente.
     */
    private boolean compruebaVigenciaCita(Cita cita){

        if(cita.getFechaConfirmacion()==null){
            return compruebaVigenciaCitaNoConfirmada(cita.getFechaCreacion());
        }else{
            return compruebaVigenciaCitaConfirmada(cita.getFechaConfirmacion());
        }

    }

    /**
     * Método que comprueba si una cita sin fecha de confirmación ha sido creada hace menos de una semana.
     *
     * @param fechaCreacion Date con la fecha de creación de la cita.
     *
     * @return Booleano true si la cita ha sido creada hace menos de una semana.
     */
    private boolean compruebaVigenciaCitaNoConfirmada(Date fechaCreacion){

        //Obtiene los días pasados entre la fecha de creación y la fecha actual.
        long dias = TimeUnit.DAYS.convert(Calendar.getInstance().getTime().getTime()
                - fechaCreacion.getTime(), TimeUnit.MILLISECONDS);

        //Comprueba si han pasado más de 7 días.
        if(dias>7){
            return false;
        }else{
            return true;
        }

    }

    /**
     * Método que comprueba si ha pasado menos de 6 horas desde que expiró la fecha confirmada de
     * realización del servicio de una cita (o sigue vigente).
     *
     * @param fechaConfirmacion Date con la fecha confirmada para la realización del servicio de la cita.
     *
     * @return Booleano true si han pasado menos de 6 horas desde la realización del servicio de la cita
     * (o sigue vigente).
     */
    private boolean compruebaVigenciaCitaConfirmada(Date fechaConfirmacion){

        //Obtiene las horas pasadas entre la fecha de confirmación y la fecha actual.
        long horas = TimeUnit.HOURS.convert(Calendar.getInstance().getTime().getTime()
                - fechaConfirmacion.getTime(), TimeUnit.MILLISECONDS);

        //Comprueba si han pasado más de 6 horas.
        if(horas>6){
            return false;
        }else{
            return true;
        }

    }

    /**
     * Método que se encarga de borrar la cita recibida en Firestore, y su respectiva foto en Storage
     * (si existe).
     *
     * @param idCita Cadena con la ID de la cita a borrar.
     */
    private void borraCita(final String idCita, final Cita cita){

        //Borra la cita recibida mediante su ID en Firestore.
        firestore.collection("citas").document(idCita).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            //Si se ha borrado la cita con éxito, se borra también el chat (si existe)...
                            borraChat(idCita);

                            //y los ficheros de la cita en Storage.
                            FirebaseStorage.getInstance().getReference("citas/"+idCita +"/perros/"
                                    +cita.getPerro().getNombre()+" "+cita.getPerro().getFechaFoto()+".jpg")
                                    .delete();

                        }
                    }
                });

    }

    /**
     * Método que borra el chat de la cita (si existe).
     *
     * @param idCita Cadena con la ID de la cita.
     */
    private void borraChat(final String idCita){

        //Obtiene todos los mensajes del chat de la cita en Firestore.
        firestore.collection("citas").document(idCita).collection("chat")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){

                    //Si el chat no está vacío...
                    if(!task.getResult().isEmpty()){

                        //borra uno por uno dichos mensajes.
                        for(QueryDocumentSnapshot doc:task.getResult()){

                            firestore.collection("citas").document(idCita)
                                    .collection("chat").document(doc.getId())
                                    .delete();

                        }

                    }

                }
            }
        });

    }

    /**
     * Método que recarga las citas.
     *
     * @param view
     */
    protected void recargaCitas(View view){

        obtieneCitas();

    }

    /**
     * Método que controla el retroceso a la activity anterior.
     *
     * @param view
     */
    protected void atras(View view){

        //Se desactivan los botones para evitar más de una pulsación...
        botonAtrasCitasConfPelu.setEnabled(false);
        botonRecargarCitasConfPelu.setEnabled(false);

        //y finaliza la activity.
        finish();

    }

}
