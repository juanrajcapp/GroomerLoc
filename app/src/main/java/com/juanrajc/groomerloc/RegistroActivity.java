package com.juanrajc.groomerloc;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.juanrajc.groomerloc.clasesBD.Cliente;

import java.util.regex.Pattern;

public class RegistroActivity extends AppCompatActivity {

    private RadioButton rbCliente, rbPeluquero;
    private EditText etRegEmail, etRegPw, etRegPw2, etRegNombre, etRegTlfn;

    private Button botonSiguiente;

    private FirebaseAuth auth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        //Instanciamos los campos de registro.
        rbCliente=(RadioButton) findViewById(R.id.rbCliente);
        rbPeluquero=(RadioButton) findViewById(R.id.rbPeluquero);

        etRegEmail=(EditText) findViewById(R.id.etRegMail);
        etRegPw=(EditText) findViewById(R.id.etRegPw);
        etRegPw2=(EditText) findViewById(R.id.etRegPw2);
        etRegNombre=(EditText) findViewById(R.id.etRegNombre);
        etRegTlfn=(EditText) findViewById(R.id.etRegTlfn);

        //Instancia el botón de siguiente.
        botonSiguiente=(Button) findViewById(R.id.botonSiguienteReg);

        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Si se vuelve a la activity, se reactivan los botones de la misma.
        botonSiguiente.setEnabled(true);


    }

    /**
     * Método que controla la validez de los campos del formulario de la primera activity de registro.
     *
     * @return Devuelve true si los campos son válidos, o false si no lo son.
     */
    protected boolean compruebaCampos(){

        //Comprueba que se ha seleccionado uno de los roles disponibles.
        if(rbCliente.isChecked() || rbPeluquero.isChecked()){

            //Comprueba que los campos de texto no están vacios.
            if(etRegEmail.getText().toString().length()>0 && etRegPw.getText().toString().length()>0 &&
                    etRegPw2.getText().toString().length()>0 && etRegNombre.getText().toString().length()>0 &&
                    etRegTlfn.getText().toString().length()>0){

                //Comprueba que el email es válido.
                if(validarEmail(etRegEmail.getText().toString())){

                    //Comprueba que las contraseñas introducidas son idénticas.
                    if(etRegPw.getText().toString().equals(etRegPw2.getText().toString())){

                        //Comprueba que las contraseñas tienen al menos 6 caracteres de longitud.
                        if(etRegPw.getText().toString().length()>=6 || etRegPw2.getText().toString().length()>=6){

                            return true;

                        }else {
                            Toast.makeText(this, getString(R.string.mensajePw2), Toast.LENGTH_SHORT).show();
                            etRegPw.setText("");
                            etRegPw2.setText("");
                        }

                    }else{
                        Toast.makeText(this, getString(R.string.mensajePw), Toast.LENGTH_SHORT).show();
                        etRegPw.setText("");
                        etRegPw2.setText("");
                    }

                }else{
                    Toast.makeText(this, getString(R.string.mensajeEmail), Toast.LENGTH_SHORT).show();
                    etRegEmail.setText("");
                }

            }else{
                Toast.makeText(this, getString(R.string.mensajeCampos), Toast.LENGTH_SHORT).show();
            }

        }else{
            Toast.makeText(this, getString(R.string.mensajeRol), Toast.LENGTH_SHORT).show();
        }

        return false;

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
     * Método que controla el inicio de la segunda activity de registro.
     *
     * @param view
     */
    protected void siguiente(View view){

        //Comprueba que los campos están correctamente completados.
        if(compruebaCampos()){

            //Se desactiva el botón de siguiente para evitar más de una pulsación.
            botonSiguiente.setEnabled(false);

            if(rbCliente.isChecked()){

                auth.createUserWithEmailAndPassword(etRegEmail.getText().toString(), etRegPw.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        auth.getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().
                                setDisplayName(etRegNombre.getText().toString()).build());

                        dbRef.child("cliente").setValue(auth.getCurrentUser().getUid());

                        dbRef.child("cliente").child(auth.getCurrentUser().getUid())
                                .setValue(new Cliente(Integer.parseInt(etRegTlfn.getText().toString()),null));

                        startActivity(new Intent(getApplicationContext(), ClienteActivity.class));

                    }
                });

            }else if(rbPeluquero.isChecked()){
                startActivity(new Intent(this, RegistroLocActivity.class));
            }

        }

    }

    /**
     * Método que controla el retroceso a la activity anterior.
     *
     * @param view
     */
    protected void atras(View view){

        finish();

    }

}
