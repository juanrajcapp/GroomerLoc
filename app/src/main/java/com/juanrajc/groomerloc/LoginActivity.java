package com.juanrajc.groomerloc;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.juanrajc.groomerloc.servicios.ServicioNotificaciones;

import java.util.regex.Pattern;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    //Constantes del tipo de usuario autenticado.
    private static final String USU_PELUQUERO="idPeluquero", USU_CLIENTE="idCliente";

    //Objetos de Firebase (Autenticación y BD Firestore).
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    //Objetos de la vista de la activity.
    private EditText email, pw;
    private Button botonLogin, botonRegistro;

    //Objeto del círculo de carga.
    private ProgressBar circuloCarga;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Le asigna el tema que mostrará la pantalla de carga.
        setTheme(R.style.AppThemeSinAB);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Instancia de los campos de login.
        email = (EditText) findViewById(R.id.mail);
        pw = (EditText) findViewById(R.id.pw);

        //Instancia los botones de la activity.
        botonLogin=(Button) findViewById(R.id.login);
        botonRegistro=(Button) findViewById(R.id.registro);

        //Instancias de la autenticación y la base de datos de Firebase.
        auth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();

        //Instancia del círculo de carga.
        circuloCarga = (ProgressBar) findViewById(R.id.circuloCarga);

    }

    @Override
    protected void onStart() {
        super.onStart();

        //Comprueba si hay un usuario autenticado.
        if (auth.getCurrentUser() != null) {

            //Si hay un usuario autenticado, se visibiliza el círculo de carga...
            circuloCarga.setVisibility(View.VISIBLE);

            //y se desactivan los botones de login y registro.
            botonLogin.setEnabled(false);
            botonRegistro.setEnabled(false);

            //Comprueba el tipo de usuario logueado e inicia la activity correspondiente a su rol.
            tipoUsuario();

        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Si se vuelve a la activity, comprueba si hay un usuario autenticado.
        if (auth.getCurrentUser() == null) {

            //si no lo hay, se para el servicio de notificaciones (si está iniciado)...
            if(isMyServiceRunning(ServicioNotificaciones.class)){
                stopService(new Intent(this, ServicioNotificaciones.class));
            }

            //se invisibiliza el círulo de carga...
            circuloCarga.setVisibility(View.INVISIBLE);

            //y se reactivan los botones de la activity.
            botonLogin.setEnabled(true);
            botonRegistro.setEnabled(true);

        }

    }

    /**
     * Método que se encarga del control del logueo del usuario.
     *
     * @param view
     */
    protected void login(View view) {

        //Si los campos de login no están correctamente rellenados...
        if (email.length() < 1 || pw.length() < 1 || !validarEmail(email.getText().toString())) {

            //se muestra un toast.
            Toast.makeText(this, getString(R.string.toast_faltaMailPw), Toast.LENGTH_SHORT).show();

        //Si están correctos...
        } else {

            //Se visibiliza el círculo de carga, ...
            circuloCarga.setVisibility(View.VISIBLE);

            //se desactiva el botón de login y registro para evitar más de una pulsación...
            botonLogin.setEnabled(false);
            botonRegistro.setEnabled(false);

            //e intenta loguear con esos datos.
            auth.signInWithEmailAndPassword(email.getText().toString(), pw.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    //Si los datos coinciden con los de algún usuario de Firebase, se loguea...
                    if (task.isSuccessful()) {

                        //borra completamente el campo de contraseña...
                        pw.setText("");

                        //y le muestra un saludo con su nombre.
                        Toast.makeText(getApplicationContext(), getString(R.string.saludo) + " " + auth.getCurrentUser()
                                .getDisplayName(), Toast.LENGTH_SHORT).show();

                        //Comprueba el tipo de usuario logueado e inicia la activity correspondiente a su rol.
                        tipoUsuario();

                    //Si no...
                    } else {

                        //Se invisibiliza el círulo de carga, ...
                        circuloCarga.setVisibility(View.INVISIBLE);

                        //muestra un toast...
                        Toast.makeText(getApplicationContext(), getString(R.string.error_autenticar), Toast.LENGTH_SHORT).show();

                        //y vuelve a activar el botón de login y registro.
                        botonLogin.setEnabled(true);
                        botonRegistro.setEnabled(true);

                    }
                }
            });
        }
    }

    /**
     * Método que se encarga de validar el email en el login.
     *
     * @param email Cadena con el email introducido por el cliente.
     * @return Devuelve true si el email es válido o false si no lo es.
     */
    private boolean validarEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    /**
     * Método que se encarga de iniciar la activity y servicio correspondiente al rol del usuario autenticado.
     */
    private void tipoUsuario(){

        //Se intenta obtener el document correspondiente al usuario logueado dentro de la colección "peluqueros"...
        firestore.collection("peluqueros").document(auth.getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    String tipoUsuario="";

                    //si existe (es un peluquero), inicia la activity del peluquero...
                    if(task.getResult().exists()){
                        tipoUsuario=USU_PELUQUERO;
                        activityPeluquero();

                    //si no (por descarte es un cliente), se inicia la activity del cliente.
                    }else{
                        activityCliente();
                        tipoUsuario=USU_CLIENTE;
                    }

                    //Comprueba si el servicio de notificaciones está activo. Lo para si es así...
                    if(isMyServiceRunning(ServicioNotificaciones.class)){
                        stopService(new Intent(getApplicationContext(), ServicioNotificaciones.class));
                    }

                    //crea el intent del servicio con los datos que va a necesitar para su ejecución...
                    Intent intentServicio = new Intent(getApplicationContext(), ServicioNotificaciones.class);
                    intentServicio.putExtra("tipoUsuario", tipoUsuario);
                    intentServicio.putExtra("idUsuario", auth.getCurrentUser().getUid());

                    //y lo inicia.
                    startService(intentServicio);

                }
            }
        });

    }

    /**
     * Método que comprueba si un servicio está en ejecución o no.
     *
     * @param serviceClass Clase del servicio a comprobar.
     *
     * @return Booleano true si el servicio está en ejecución.
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Método que se encarga de controlar el inicio de la primera activity de registro.
     *
     * @param view
     */
    protected void activityRegistro(View view){

        //Se desactiva el botón de registro y login para evitar más de una pulsación.
        botonRegistro.setEnabled(false);
        botonLogin.setEnabled(false);

        //Se inicia la activity de registro.
        startActivity(new Intent(this, RegistroActivity.class));

    }

    /**
     * Método que controla el inicio de la activity del cliente.
     */
    private void activityCliente(){

        //Se inicia la activity de cliente.
        startActivity(new Intent(this, ClienteActivity.class));

    }

    /**
     * Método que controla el inicio de la activity del peluquero.
     */
    private void activityPeluquero(){

        //Se inicia la activity de peluquero.
        startActivity(new Intent(this, PeluqueroActivity.class));

    }

}