package com.juanrajc.groomerloc;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private EditText email, pw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {

            FirebaseUser usuarioActual = auth.getCurrentUser();

            Toast.makeText(this, getString(R.string.saludo) + usuarioActual.getDisplayName(), Toast.LENGTH_SHORT).show();
            activityCliente();

        }

    }

    protected void login(final View view) {

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

    private boolean validarEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    private void activityCliente(){

        startActivity(new Intent(this, ClienteActivity.class));

    }

}