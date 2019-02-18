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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.juanrajc.groomerloc.adaptadores.AdaptadorCitasCliente;
import com.juanrajc.groomerloc.clasesBD.Cita;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CitasClienteActivity extends AppCompatActivity {

    //Objeto del adaptador que muestra las fichas de las citas del cliente.
    private RecyclerView rvCitasCliente;

    //Objeto del usuario actual y de la BD Firestore.
    private FirebaseUser usuario;
    private FirebaseFirestore firestore;

    //Objeto del botón para recargar las citas del cliente y para retroceder.
    private Button botonRecargaCitasCliente, botonAtrasCitasCliente;

    //Objeto del círculo de carga.
    private ProgressBar circuloCargaCitasCliente;

    //Objeto del EditText que aparece cuando no hay citas.
    private TextView tvNoCitasCliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citas_cliente);

        //Instancia del usuario actual y de la base de datos Firestore.
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        //Instancia del botón para recargar citas del cliente y para retroceder.
        botonRecargaCitasCliente = (Button) findViewById(R.id.botonRecargaCitasCliente);
        botonAtrasCitasCliente = (Button) findViewById(R.id.botonAtrasCitasCliente);

        //Instancia del círculo de carga.
        circuloCargaCitasCliente = (ProgressBar) findViewById(R.id.circuloCargaCitasCliente);

        //Instancia del EditText que aparece cuando no hay citas.
        tvNoCitasCliente = (TextView) findViewById(R.id.tvNoCitasCliente);

        //Instancia del RecyclerView de citas del cliente.
        rvCitasCliente = (RecyclerView) findViewById(R.id.rvCitasCliente);

        //Fija el tamaño del rv, que mejorará el rendimento.
        rvCitasCliente.setHasFixedSize(true);

        //Administrador para el LinearLayout.
        rvCitasCliente.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Se oculta el mensaje de la no existencia de citas...
        tvNoCitasCliente.setVisibility(View.INVISIBLE);

        //y se obtienen las citas del cliente.
        obtieneCitasCliente();

    }

    /**
     * Método que obtiene las citas creadas por el cliente actual desde Firebase Firestore.
     */
    private void obtieneCitasCliente(){

        //Se desactivan los botones de la activity para evitar dobles pulsaciones.
        botonRecargaCitasCliente.setEnabled(false);
        botonAtrasCitasCliente.setEnabled(false);

        //Se visibiliza el círculo de carga.
        circuloCargaCitasCliente.setVisibility(View.VISIBLE);

        //Obtiene las citas credas por el cliente actual en orden de creación descendente desde Firestore.
        firestore.collection("citas").whereEqualTo("idCliente", usuario.getUid())
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                //Si se obtienen resultados satisfactoriamente...
                if(task.isSuccessful()){

                    //comprueba que esos resultados contienen datos.
                    if(task.getResult().isEmpty()){
                        rvCitasCliente.setAdapter(new AdaptadorCitasCliente(null, null));
                        tvNoCitasCliente.setVisibility(View.VISIBLE);
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
                            if(compruebaVigenciaCita(doc.toObject(Cita.class))){

                                listaIdsCitas.add(doc.getId());
                                listaObjCitas.add(doc.toObject(Cita.class));

                            }else{

                                borraCita(doc.getId(), doc.toObject(Cita.class));

                            }

                        }

                        /*
                        Si no se ha añadido ninguna cita al list, se muestra un mensaje en
                        la vista de la activity.
                        */
                        if(listaIdsCitas.isEmpty()){
                            tvNoCitasCliente.setVisibility(View.VISIBLE);
                        }

                        //Crea un nuevo adaptador con las citas obtenidas.
                        rvCitasCliente.setAdapter(new AdaptadorCitasCliente(listaIdsCitas, listaObjCitas));

                    }

                /*
                Si ha habido algún problema al obtener resultados,
                se invisibiliza el círculo de carga y se muestra un toast avisándolo.
                */
                }else{
                    circuloCargaCitasCliente.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), getString(R.string.mensajeNoResultCitas),
                            Toast.LENGTH_SHORT).show();
                }

                //Finalizada la carga, se vuelve a invisibilizar el círculo de carga y a activar los botones.
                circuloCargaCitasCliente.setVisibility(View.INVISIBLE);
                botonRecargaCitasCliente.setEnabled(true);
                botonAtrasCitasCliente.setEnabled(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                circuloCargaCitasCliente.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), getString(R.string.mensajeNoResultCitas),
                        Toast.LENGTH_SHORT).show();
                botonRecargaCitasCliente.setEnabled(true);
                botonAtrasCitasCliente.setEnabled(true);
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
     * Método que recarga las citas creadas por el cliente.
     *
     * @param view
     */
    public void recargaCitasCliente(View view){

        obtieneCitasCliente();

    }

    /**
     * Método que controla el retroceso a la activity anterior.
     *
     * @param view
     */
    public void atras(View view){

        //Se desactivan los botones para evitar más de una pulsación...
        botonAtrasCitasCliente.setEnabled(false);
        botonRecargaCitasCliente.setEnabled(false);

        //y finaliza la activity.
        finish();

    }

}
