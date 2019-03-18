package com.juanrajc.groomerloc.servicios;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.juanrajc.groomerloc.R;
import com.juanrajc.groomerloc.clasesBD.Cita;
import com.juanrajc.groomerloc.clasesBD.Cliente;
import com.juanrajc.groomerloc.clasesBD.Mensaje;
import com.juanrajc.groomerloc.clasesBD.Peluquero;
import com.juanrajc.groomerloc.notificaciones.AsistenteNotificaciones;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

public class ServicioNotificaciones extends Service {

    //Constantes del tipo de usuario autenticado.
    public static final String USU_PELUQUERO = "idPeluquero", USU_CLIENTE = "idCliente";

    //Constantes del tipo de notificación.
    private final int NOT_CITAS = 1, NOT_FECHAS = 2, NOT_MENSAJES = 3;

    //Tipo e ID del usuario actual.
    private String tipoUsuario, idUsuario;

    //Fecha y hora de creación del servicio.
    private Date dateServicio;

    //Lista de listeners iniciados por el servicio.
    private List<ListenerRegistration> listaListenersCitas, listaListenersChats;

    //Objeto de la instancia de Firestore.
    private FirebaseFirestore firestore;

    //Objeto del asistente de notificaciones.
    private AsistenteNotificaciones asistenteNotificaciones;

    /**
     * Constructor por defecto del servicio de notificaciones.
     */
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

        //Instancias de las listas de listeners.
        listaListenersCitas = new ArrayList<ListenerRegistration>();
        listaListenersChats = new ArrayList<ListenerRegistration>();

        //Instancia del asistente de notificaciones.
        asistenteNotificaciones = new AsistenteNotificaciones(getApplicationContext());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Instancia de la fecha y hora de creación del servicio.
        dateServicio = Calendar.getInstance().getTime();

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

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Elimina todos los listeners de chats registrados.
        for (ListenerRegistration listenerChat : listaListenersChats) {
            listenerChat.remove();
        }

        //Elimina todos los listeners de citas registradas.
        for (ListenerRegistration listenerCita : listaListenersCitas) {
            listenerCita.remove();
        }

    }

    /**
     * Método que se encarga de crear el listener que recoge los eventos producidos en las citas
     * del usuario actual.
     */
    private void listenerCitas() {

        /*
        Crea el listener que recoge los eventos producidos en las citas del usuario especificado y
        lo añade a la lista de listeners de citas.
        */
        listaListenersCitas.add(firestore.collection("citas").whereEqualTo(tipoUsuario, idUsuario)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {

                        //Comprueba si el QDS está vacío.
                        if (!queryDocumentSnapshots.isEmpty()) {

                            //Obtiene los cambios producidos.
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                                //Crea un objeto de tipo Cita.
                                Cita cita = doc.getDocument().toObject(Cita.class);

                                switch (doc.getType()) {

                                    //Si se añadió un elemento.
                                    case ADDED:

                                        //Cadena que contendrá la ID del emisor respecto al usuario actual.
                                        String idUsuarioEmisor = "";

                                        //Comprueba el tipo de usuario actualmente autenticado.
                                        if (tipoUsuario.equals(USU_PELUQUERO)) {

                                            //Controla la actualidad del evento.
                                            if (controlaActualidadElemento(cita.getFechaCreacion())) {

                                                cargaNombreUsuario("clientes",
                                                        cita.getIdCliente(), cita.getFechaCreacion(),
                                                        doc.getDocument().getId());

                                            }

                                            idUsuarioEmisor = cita.getIdCliente();

                                        } else if (tipoUsuario.equals(USU_CLIENTE)) {

                                            idUsuarioEmisor = cita.getIdPeluquero();

                                        }

                                        //Crea un listener para el chat de cada cita.
                                        listenerChatCita(doc.getDocument().getId(),
                                                idUsuarioEmisor);

                                        break;

                                    //Si se modificó un elemento.
                                    case MODIFIED:

                                        //Si el usuario es un cliente...
                                        if (tipoUsuario.equals(USU_CLIENTE)) {

                                            cargaNombreUsuario("peluqueros",
                                                    cita.getIdPeluquero(), cita.getFechaConfirmacion(),
                                                    doc.getDocument().getId());

                                        }

                                        break;

                                    //Si se eliminó un elemento.
                                    case REMOVED:
                                }

                            }

                        }

                    }

                }));

    }

    /**
     * Método que crea un listener que recoge los eventos producidos en el chat de la cita obtenida
     * por parámetro.
     *
     * @param idCita          Cadena con la ID de la cita.
     * @param idUsuarioEmisor Cadena con la ID del usuario emisor respecto al usuario actual.
     */
    private void listenerChatCita(final String idCita, final String idUsuarioEmisor) {

        /*
        Crea el listener que recoge los eventos producidos en el chat de la cita especificada y
        lo añade a la lista de listeners de chats.
        */
        listaListenersChats.add(firestore.collection("citas").document(idCita).collection("chat")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {

                        //Comprueba si el QDS está vacío.
                        if (!queryDocumentSnapshots.isEmpty()) {

                            //Obtiene los cambios producidos.
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                                //Crea un objeto de tipo Mensaje.
                                Mensaje mensaje = doc.getDocument().toObject(Mensaje.class);

                                switch (doc.getType()) {

                                    //Si se añadió un elemento.
                                    case ADDED:

                                        /*
                                        Controla la actualidad del evento y si la aplicación
                                        está o no en primer plano.
                                        */
                                        if (controlaActualidadElemento(mensaje.getFecha()) &&
                                                !isAppOnForeground(getApplicationContext(),
                                                        "com.juanrajc.groomerloc")) {

                                            //Comprueba si la versión de Android es igual o superior a la 8.0.
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                                                //Crea una notificación de mensaje recibido (Android 8.0+).
                                                asistenteNotificaciones
                                                        .notify(generaEnteroUsuario(NOT_MENSAJES,
                                                                idUsuarioEmisor),
                                                                asistenteNotificaciones.getNotificationMensajes(
                                                                        getString(R.string.tituloMensajeNuevo)
                                                                                + " " + mensaje.getUsuario() + " | "
                                                                                + new SimpleDateFormat("dd/MM/yyyy HH:mm")
                                                                                .format(mensaje.getFecha()),
                                                                        mensaje.getTexto(), tipoUsuario, idCita));

                                            } else {

                                                //Crea una notificación de mensaje recibido.
                                                asistenteNotificaciones
                                                        .notifyOld(generaEnteroUsuario(NOT_MENSAJES,
                                                                idUsuarioEmisor),
                                                                asistenteNotificaciones.getNotificationMensajesOld(
                                                                        getString(R.string.tituloMensajeNuevo)
                                                                                + " " + mensaje.getUsuario() + " | "
                                                                                + new SimpleDateFormat("dd/MM/yyyy HH:mm")
                                                                                .format(mensaje.getFecha()),
                                                                        mensaje.getTexto(), tipoUsuario, idCita));

                                            }

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

                }));

    }

    /**
     * Método que carga los datos del usuario que creó una cita (cliente) o estableció una fecha para la
     * realización del servicio (peluquero). Se obtiene el nombre.
     *
     * @param usuarioFirestore Cadena con el nombre de la colección del tipo de usuario en Firestore.
     * @param idUsuario        Cadena con la ID del usuario del cual vamos a obtener los datos.
     * @param fecha            Date con la fecha y hora de realización del evento.
     * @param idCita           Cadena con la ID de la cita donde se produce el evento.
     */
    private void cargaNombreUsuario(String usuarioFirestore, final String idUsuario, final Date fecha,
                                    final String idCita) {

        //Obtiene los datos del usuario recibido en Firestore.
        firestore.collection(usuarioFirestore).document(idUsuario).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            //Comprueba el tipo de usuario actualmente autenticado.
                            if (tipoUsuario.equals(USU_PELUQUERO)) {

                                //Crea un objeto de tipo Cliente.
                                Cliente cliente = task.getResult().toObject(Cliente.class);

                                //Comprueba si la versión de Android es igual o superior a la 8.0.
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                                    //Crea una notificación de nueva cita para el peluquero (Android 8.0+).
                                    asistenteNotificaciones.notify(generaEnteroUsuario(NOT_CITAS,
                                            idUsuario), asistenteNotificaciones.getNotificationCitas(
                                            getString(R.string.tituloCitaNueva) + " "
                                                    + cliente.getNombre() + " | "
                                                    + new SimpleDateFormat("dd/MM/yyyy HH:mm")
                                                    .format(fecha),
                                            getString(R.string.citasCliente) + " " + cliente.getNombre()
                                                    + "\n" + getString(R.string.cvCitasPeluFechaCrea) + " "
                                                    + new SimpleDateFormat("dd/MM/yyyy HH:mm")
                                                    .format(fecha), idCita));

                                } else {

                                    //Crea una notificación de nueva cita para el peluquero.
                                    asistenteNotificaciones.notifyOld(generaEnteroUsuario(NOT_CITAS,
                                            idUsuario), asistenteNotificaciones.getNotificationCitasOld(
                                            getString(R.string.tituloCitaNueva) + " "
                                                    + cliente.getNombre() + " | "
                                                    + new SimpleDateFormat("dd/MM/yyyy HH:mm")
                                                    .format(fecha),
                                            getString(R.string.citasCliente) + " " + cliente.getNombre()
                                                    + "\n" + getString(R.string.cvCitasPeluFechaCrea) + " "
                                                    + new SimpleDateFormat("dd/MM/yyyy HH:mm")
                                                    .format(fecha), idCita));

                                }

                            } else if (tipoUsuario.equals(USU_CLIENTE)) {

                                //Crea un objeto de tipo Peluquero.
                                Peluquero peluquero = task.getResult().toObject(Peluquero.class);

                                //Comprueba si la versión de Android es igual o superior a la 8.0.
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                                    /*
                                    Crea una notificación de fecha establecida por el peluquero
                                    para el cliente (Android 8.0+).
                                    */
                                    asistenteNotificaciones.notify(generaEnteroUsuario(NOT_FECHAS,
                                            idUsuario), asistenteNotificaciones.getNotificationFechas(
                                            getString(R.string.tituloFechaEstablecida) + " "
                                                    + peluquero.getNombre() + " | "
                                                    + new SimpleDateFormat("dd/MM/yyyy HH:mm")
                                                    .format(fecha),
                                            getString(R.string.mensajeFechaEstablecidaPelu) + " "
                                                    + peluquero.getNombre() + "\n"
                                                    + getString(R.string.cvCitasPeluFechaConf) + " "
                                                    + new SimpleDateFormat("dd/MM/yyyy HH:mm")
                                                    .format(fecha), idCita));

                                } else {

                                    /*
                                    Crea una notificación de fecha establecida por el peluquero
                                    para el cliente.
                                    */
                                    asistenteNotificaciones.notifyOld(generaEnteroUsuario(NOT_FECHAS,
                                            idUsuario), asistenteNotificaciones.getNotificationFechasOld(
                                            getString(R.string.tituloFechaEstablecida) + " "
                                                    + peluquero.getNombre() + " | "
                                                    + new SimpleDateFormat("dd/MM/yyyy HH:mm")
                                                    .format(fecha),
                                            getString(R.string.mensajeFechaEstablecidaPelu) + " "
                                                    + peluquero.getNombre() + "\n"
                                                    + getString(R.string.cvCitasPeluFechaConf) + " "
                                                    + new SimpleDateFormat("dd/MM/yyyy HH:mm")
                                                    .format(fecha), idCita));

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

    /**
     * Comprueba si la aplicación recibida está o no en primer plano.
     *
     * @param context        Contexto de la aplicación.
     * @param appPackageName Cadena con el nombre del paquete de la aplicación.
     * @return Booleano true si la aplicación está en primer plano.
     */
    private boolean isAppOnForeground(Context context, String appPackageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = appPackageName;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Método que genera un entero a partir del tipo de notificación y la ID del usuario.
     *
     * @param tipoNotificacion Entero con el valor del tipo de notificación.
     * @param idUsuario        Cadena con la ID del usuario.
     * @return Entero con el valor calculado.
     */
    private int generaEnteroUsuario(int tipoNotificacion, String idUsuario) {

        int valor = tipoNotificacion;

        for (char caracter : idUsuario.toCharArray()) {
            valor += (int) caracter;
        }

        return valor;

    }

}
