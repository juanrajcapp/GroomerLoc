package com.juanrajc.groomerloc;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    //Objeto de Firebase Authentication
    private FirebaseAuth auth;

    //Objetos de la vista de la activity.
    private EditText email, pw;
    private Button botonLogin, botonRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Le asigna el tema que mostrará la pantalla de carga.
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Instancia los botones de la activity.
        botonLogin=(Button) findViewById(R.id.login);
        botonRegistro=(Button) findViewById(R.id.registro);

        //Recoje la instancia de autenticación de la clase FirebaseApp.
        auth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();

        //Comprueba si hay un usuario autenticado.
        if (auth.getCurrentUser() != null) {

            //Inicia la activity del cliente.
            activityCliente();

        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Si se vuelve a la activity, se reactivan los botones de la misma.
        botonLogin.setEnabled(true);
        botonRegistro.setEnabled(true);


    }

    /**
     * Método que se encarga del control del logueo del usuario.
     *
     * @param view
     */
    protected void login(View view) {

        //Instancia de los campos de login.
        email = (EditText) findViewById(R.id.mail);
        pw = (EditText) findViewById(R.id.pw);


        if (email.length() < 1 || pw.length() < 1 || !validarEmail(email.getText().toString())) {

            Toast.makeText(this, getString(R.string.toast_faltaMailPw), Toast.LENGTH_SHORT).show();

        } else {

            auth.signInWithEmailAndPassword(email.getText().toString(), pw.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                activityCliente();

                            } else {

                                Toast.makeText(getApplicationContext(), getString(R.string.error_autenticar), Toast.LENGTH_SHORT).show();

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
     * Método que se encarga de controlar el inicio de la primera activity de registro.
     *
     * @param view
     */
    protected void activityRegistro(View view){

        //Se desactiva el botón de registro para evitar más de una pulsación.
        botonRegistro.setEnabled(false);

        //Se inicia la activity de registro.
        startActivity(new Intent(this, RegistroActivity.class));

    }

    /**
     * Método que controla el inicio de la activity del cliente.
     */
    private void activityCliente(){

        //Se desactiva el botón de login para evitar más de una pulsación.
        botonLogin.setEnabled(false);

        //Se inicia la activity de cliente.
        startActivity(new Intent(this, ClienteActivity.class));

    }

}