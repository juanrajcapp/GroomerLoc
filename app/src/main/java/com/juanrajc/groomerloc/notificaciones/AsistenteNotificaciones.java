package com.juanrajc.groomerloc.notificaciones;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.juanrajc.groomerloc.CitaClienteActivity;
import com.juanrajc.groomerloc.CitaPeluActivity;
import com.juanrajc.groomerloc.LoginActivity;
import com.juanrajc.groomerloc.R;
import com.juanrajc.groomerloc.servicios.ServicioNotificaciones;

public class AsistenteNotificaciones extends ContextWrapper {

    //Objeto del manager de notificaciones (Android 8.0+).
    private NotificationManager notificationManager;

    //Objeto del manager de notificaciones
    private NotificationManagerCompat notificationManagerCompat;

    //Constantes con las IDs de los canales de notificación.
    public static final String
            CANAL_CITAS_ID = "com.juanrajc.groomerloc.CITAS",
            CANAL_FECHAS_ID = "com.juanrajc.groomerloc.FECHA",
            CANAL_MENSAJES_ID = "com.juanrajc.groomerloc.MENSAJES";


    public AsistenteNotificaciones(Context base) {
        super(base);

        //Si la versión de Android es 8.0 o mayor...
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            creaCanales();
        }

    }

    /**
     * Método que crea los canales de notificación para Android 8.0+.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void creaCanales(){

        //Instancia del canal de citas nuevas para el peluquero con su configuración.
        NotificationChannel notificationChannelCitas = new NotificationChannel(CANAL_CITAS_ID,
                getString(R.string.categoriaCanalCitas), notificationManager.IMPORTANCE_DEFAULT);
        notificationChannelCitas.enableLights(true);
        notificationChannelCitas.setLightColor(Color.RED);
        notificationChannelCitas.enableVibration(true);
        notificationChannelCitas.setShowBadge(true);
        notificationChannelCitas.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        getManager().createNotificationChannel(notificationChannelCitas);

        //Instancia del canal de fechas confirmadas por el peluquero para el cliente con su configuración.
        NotificationChannel notificationChannelFechas = new NotificationChannel(CANAL_FECHAS_ID,
                getString(R.string.categoriaCanalFechas), notificationManager.IMPORTANCE_DEFAULT);
        notificationChannelFechas.enableLights(true);
        notificationChannelFechas.setLightColor(Color.YELLOW);
        notificationChannelFechas.enableVibration(true);
        notificationChannelFechas.setShowBadge(true);
        notificationChannelFechas.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        getManager().createNotificationChannel(notificationChannelFechas);

        //Instancia del canal de mensajes de chat para los usuarios con su configuración.
        NotificationChannel notificationChannelMensajes = new NotificationChannel(CANAL_MENSAJES_ID,
                getString(R.string.categoriaCanalMensajes), notificationManager.IMPORTANCE_DEFAULT);
        notificationChannelMensajes.enableLights(true);
        notificationChannelMensajes.setLightColor(Color.GREEN);
        notificationChannelMensajes.enableVibration(false);
        notificationChannelMensajes.setShowBadge(false);
        notificationChannelMensajes.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        getManager().createNotificationChannel(notificationChannelMensajes);

    }

    /**
     * Método que crea y devuelve una notificación de cita nueva (Android 8.0+).
     *
     * @param title Cadena con el título de la notificación.
     * @param body Cadena con el el contenido de la notificación.
     * @param idCita Cadena con la ID de la cita a mostrar cuando se pulse la notificación.
     *
     * @return Notificación lista para ser mostrada.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotificationCitas(String title, String body, String idCita) {
        return new Notification.Builder(getApplicationContext(), CANAL_CITAS_ID)
                //Título.
                .setContentTitle(title)
                //Texto de la notificación.
                .setContentText(body)
                //Categoría de la notificación.
                .setCategory(Notification.CATEGORY_SOCIAL)
                .setGroupSummary(true)
                //Grupo de notificaciones al que pertenece.
                .setGroup(CANAL_CITAS_ID)
                //Icono que se muestra en la barra de notificaciones.
                .setSmallIcon(R.drawable.logo_groomerloc_notificacion_mas)
                //Notificación con texto expandible.
                .setStyle(new Notification.BigTextStyle().bigText(body))
                //Se autoelimina cuando se pulsa sobre ella.
                .setAutoCancel(true)
                //Acción que realiza al ser pulsada.
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        obtieneIntent(ServicioNotificaciones.USU_PELUQUERO, idCita),
                        PendingIntent.FLAG_UPDATE_CURRENT));
    }

    /**
     * Método que crea y devuelve una notificación de cita nueva.
     *
     * @param title Cadena con el título de la notificación.
     * @param body Cadena con el el contenido de la notificación.
     * @param idCita Cadena con la ID de la cita a mostrar cuando se pulse la notificación.
     *
     * @return Notificación lista para ser mostrada.
     */
    public NotificationCompat.Builder getNotificationCitasOld(String title, String body, String idCita) {
        return new NotificationCompat.Builder(getApplicationContext(), CANAL_CITAS_ID)
                //Título.
                .setContentTitle(title)
                //Texto de la notificación.
                .setContentText(body)
                //Categoría de la notificación.
                .setCategory(Notification.CATEGORY_SOCIAL)
                .setGroupSummary(true)
                //Grupo de notificaciones al que pertenece.
                .setGroup(CANAL_CITAS_ID)
                //Icono que se muestra en la barra de notificaciones.
                .setSmallIcon(R.drawable.logo_groomerloc_notificacion_mas)
                //Notificación con texto expandible.
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                //Se autoelimina cuando se pulsa sobre ella.
                .setAutoCancel(true)
                //Acción que realiza al ser pulsada.
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        obtieneIntent(ServicioNotificaciones.USU_PELUQUERO, idCita),
                        PendingIntent.FLAG_UPDATE_CURRENT));
    }

    /**
     * Método que crea y devuelve una notificación de fecha establecida (Android 8.0+).
     *
     * @param title Cadena con el título de la notificación.
     * @param body Cadena con el el contenido de la notificación.
     * @param idCita Cadena con la ID de la cita a mostrar cuando se pulse la notificación.
     *
     * @return Notificación lista para ser mostrada.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotificationFechas(String title, String body, String idCita) {
        return new Notification.Builder(getApplicationContext(), CANAL_FECHAS_ID)
                //Título.
                .setContentTitle(title)
                //Texto de la notificación.
                .setContentText(body)
                //Categoría de la notificación.
                .setCategory(Notification.CATEGORY_EVENT)
                .setGroupSummary(true)
                //Grupo de notificaciones al que pertenece.
                .setGroup(CANAL_FECHAS_ID)
                //Icono que se muestra en la barra de notificaciones.
                .setSmallIcon(R.drawable.logo_groomerloc_notificacion)
                //Notificación con texto expandible.
                .setStyle(new Notification.BigTextStyle().bigText(body))
                //Se autoelimina cuando se pulsa sobre ella.
                .setAutoCancel(true)
                //Acción que realiza al ser pulsada.
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        obtieneIntent(ServicioNotificaciones.USU_CLIENTE, idCita),
                        PendingIntent.FLAG_UPDATE_CURRENT));
    }

    /**
     * Método que crea y devuelve una notificación de fecha establecida.
     *
     * @param title Cadena con el título de la notificación.
     * @param body Cadena con el el contenido de la notificación.
     * @param idCita Cadena con la ID de la cita a mostrar cuando se pulse la notificación.
     *
     * @return Notificación lista para ser mostrada.
     */
    public NotificationCompat.Builder getNotificationFechasOld(String title, String body, String idCita) {
        return new NotificationCompat.Builder(getApplicationContext(), CANAL_FECHAS_ID)
                //Título.
                .setContentTitle(title)
                //Texto de la notificación.
                .setContentText(body)
                //Categoría de la notificación.
                .setCategory(Notification.CATEGORY_EVENT)
                .setGroupSummary(true)
                //Grupo de notificaciones al que pertenece.
                .setGroup(CANAL_FECHAS_ID)
                //Icono que se muestra en la barra de notificaciones.
                .setSmallIcon(R.drawable.logo_groomerloc_notificacion)
                //Notificación con texto expandible.
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                //Se autoelimina cuando se pulsa sobre ella.
                .setAutoCancel(true)
                //Acción que realiza al ser pulsada.
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        obtieneIntent(ServicioNotificaciones.USU_CLIENTE, idCita),
                        PendingIntent.FLAG_UPDATE_CURRENT));
    }

    /**
     * Método que crea y devuelve una notificación de mensaje nuevo (Android 8.0+).
     *
     * @param title Cadena con el título de la notificación.
     * @param body Cadena con el el contenido de la notificación.
     * @param tipoUsuario Cadena con el tipo de usuario autenticado (cliente o peluquero).
     * @param idCita Cadena con la ID de la cita a mostrar cuando se pulse la notificación.
     *
     * @return Notificación lista para ser mostrada.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotificationMensajes(String title, String body, String tipoUsuario, String idCita) {
        return new Notification.Builder(getApplicationContext(), CANAL_MENSAJES_ID)
                //Título.
                .setContentTitle(title)
                //Texto de la notificación.
                .setContentText(body)
                //Categoría de la notificación.
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setGroupSummary(true)
                //Grupo de notificaciones al que pertenece.
                .setGroup(CANAL_MENSAJES_ID)
                //Icono que se muestra en la barra de notificaciones.
                .setSmallIcon(R.drawable.logo_groomerloc_notificacion)
                //Notificación con texto expandible.
                .setStyle(new Notification.BigTextStyle().bigText(body))
                //Se autoelimina cuando se pulsa sobre ella.
                .setAutoCancel(true)
                //Acción que realiza al ser pulsada.
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        obtieneIntent(tipoUsuario, idCita), PendingIntent.FLAG_UPDATE_CURRENT));
    }

    /**
     * Método que crea y devuelve una notificación de mensaje nuevo.
     *
     * @param title Cadena con el título de la notificación.
     * @param body Cadena con el el contenido de la notificación.
     * @param tipoUsuario Cadena con el tipo de usuario autenticado (cliente o peluquero).
     * @param idCita Cadena con la ID de la cita a mostrar cuando se pulse la notificación.
     *
     * @return Notificación lista para ser mostrada.
     */
    public NotificationCompat.Builder getNotificationMensajesOld(String title, String body, String tipoUsuario, String idCita) {
        return new NotificationCompat.Builder(getApplicationContext(), CANAL_MENSAJES_ID)
                //Título.
                .setContentTitle(title)
                //Texto de la notificación.
                .setContentText(body)
                //Categoría de la notificación.
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setGroupSummary(true)
                //Grupo de notificaciones al que pertenece.
                .setGroup(CANAL_MENSAJES_ID)
                //Icono que se muestra en la barra de notificaciones.
                .setSmallIcon(R.drawable.logo_groomerloc_notificacion)
                //Notificación con texto expandible.
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                //Se autoelimina cuando se pulsa sobre ella.
                .setAutoCancel(true)
                //Acción que realiza al ser pulsada.
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        obtieneIntent(tipoUsuario, idCita), PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public void notify(int id, Notification.Builder notification) {
        getManager().notify(id, notification.build());
    }

    public void notifyOld(int id, NotificationCompat.Builder notificationCompat) {
        getManagerOld().notify(id, notificationCompat.build());
    }

    private NotificationManager getManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    private NotificationManagerCompat getManagerOld() {
        if (notificationManagerCompat == null) {
            notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        }
        return notificationManagerCompat;
    }

    /**
     * Método que crea el intent para el rol correspondiente.
     *
     * @param tipoUsuario Cadena con el tipo de usuario autenticado (cliente o peluquero).
     * @param idCita Cadena con la ID de la cita que va a mostrar.
     *
     * @return Intent con la activity y cita a mostrar.
     */
    private Intent obtieneIntent(String tipoUsuario, String idCita){

        Intent intent = null;

        switch (tipoUsuario){

            case ServicioNotificaciones.USU_CLIENTE:

                intent = new Intent(this, CitaClienteActivity.class);
                intent.putExtra("idCita", idCita);

                return intent;

            case ServicioNotificaciones.USU_PELUQUERO:

                intent = new Intent(this, CitaPeluActivity.class);
                intent.putExtra("idCita", idCita);

                return intent;

                default:

                    return new Intent(this, LoginActivity.class);

        }

    }

}
