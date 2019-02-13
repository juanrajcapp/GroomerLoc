package com.juanrajc.groomerloc.servicios;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.juanrajc.groomerloc.clasesBD.Cita;
import com.juanrajc.groomerloc.clasesBD.Mensaje;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

public class ServicioNotificaciones extends Service {

    //Constantes del tipo de usuario autenticado.
    private static final String USU_PELUQUERO = "idPeluquero", USU_CLIENTE = "idCliente";

    //Tipo e ID del usuario actual.
    private String tipoUsuario, idUsuario;

    //Fecha y hora de creación del servicio.
    private Date dateServicio;

    //Objeto de la instancia de Firestore.
    private FirebaseFirestore firestore;

    public ServicioNotificaciones() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Instancia de la BD de Firestore.
        firestore = FirebaseFirestore.getInstance();

        //Instancia de la fecha y hora de creación del servicio.
        dateServicio = Calendar.getInstance().getTime();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Obtiene el tipo e ID del usuario del intent recibido.
        tipoUsuario = intent.getStringExtra("tipoUsuario");
        idUsuario = intent.getStringExtra("idUsuario");

        //Comprueba que el tipo de usuario es un tipo válido.
        if (tipoUsuario.equals(USU_PELUQUERO) || tipoUsuario.equals(USU_CLIENTE)) {
            listenerCitas();
        }

        //Si se para el servicio, se vuelve a iniciar con los valores anteriormente recibidos.
        return START_REDELIVER_INTENT;
    }

    /**
     * Método que se encarga de crear el listener que recoge los eventos producidos en las citas
     * del usuario actual.
     */
    private void listenerCitas() {

        //Crea el listener que recoge los eventos producidos en las citas del usuario especificado.
        firestore.collection("citas").whereEqualTo(tipoUsuario, idUsuario)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {

                        //Comprueba si el QDS está vacío.
                        if (!queryDocumentSnapshots.isEmpty()) {

                            //Obtiene los cambios producidos.
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                                switch (doc.getType()) {

                                    //Si se añadió un elemento.
                                    case ADDED:

                                        //Controla la actualidad del evento.
                                        if (controlaActualidadElemento(doc.getDocument()
                                                .toObject(Cita.class).getFechaCreacion())) {

                                            //Si el usuario es un peluquero...
                                            if (tipoUsuario.equals(USU_PELUQUERO)) {

                                                //NOTIFICACION

                                            }

                                        }

                                        //Crea un listener para el chat de cada cita.
                                        listenerChatCita(doc.getDocument().getId());

                                        break;

                                    //Si se modificó un elemento.
                                    case MODIFIED:

                                        //Si el usuario es un cliente...
                                        if (tipoUsuario.equals(USU_CLIENTE)) {

                                            //NOTIFICACION

                                        }

                                        break;

                                    //Si se eliminó un elemento.
                                    case REMOVED:
                                }

                            }

                        }

                    }

                });

    }

    /**
     * Método que crea un listener que recoge los eventos producidos en el chat de la cita obtenida
     * por parámetro.
     *
     * @param idCita Cadena con la ID de la cita.
     */
    private void listenerChatCita(String idCita) {

        //Crea el listener que recoge los eventos producidos en el chat de la cita especificada.
        firestore.collection("citas").document(idCita).collection("chat")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {

                        //Comprueba si el QDS está vacío.
                        if (!queryDocumentSnapshots.isEmpty()) {

                            //Obtiene los cambios producidos.
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                                switch (doc.getType()) {

                                    //Si se añadió un elemento.
                                    case ADDED:

                                        //Controla la actualidad del evento.
                                        if (controlaActualidadElemento(doc.getDocument()
                                                .toObject(Mensaje.class).getFecha())) {

                                            /*Notification notificacion = new NotificationCompat
                                                    .Builder(getApplicationContext(), ¿?)
                                                    .setSmallIcon(R.drawable.icono_loc_persona)
                                                    .setContentTitle("Mensaje de " + doc.getDocument().toObject(Mensaje.class).getUsuario())
                                                    .setContentText(doc.getDocument().toObject(Mensaje.class).getTexto())
                                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT).build();

                                            startForeground(1, notificacion);*/

                                        }

                                        break;

                                    //Si se modificó un elemento.
                                    case MODIFIED:
                                        break;

                                    //Si se eliminó un elemento.
                                    case REMOVED:
                                }

                            }

                        }

                    }

                });

    }

    /**
     * Método que comprueba si la fecha y hora de creación del elemento es posterior a la fecha
     * y hora de creación del servicio.
     *
     * @param dateElemento Date con la fecha y hora que se va a comprobar.
     *
     * @return Booleano true si la fecha y hora recibida es posterior a la de creación del servicio.
     */
    private boolean controlaActualidadElemento(Date dateElemento) {

        /*
        Obtiene los minutos pasados entre la fecha y hora de creación del servicio y la
        fecha y hora de creación del elemento.
        */
        long minutos = TimeUnit.MINUTES.convert(dateElemento.getTime()
                - dateServicio.getTime(), TimeUnit.MILLISECONDS);

        /*
        Comprueba si los minutos pasados son negativos (en ese caso retornaría falso porque
        significaría que la fecha del elemento es posterior a la creación del servicio).
        */
        if (minutos < 0) {
            return false;
        } else {
            return true;
        }

    }

}
