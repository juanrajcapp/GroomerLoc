package com.juanrajc.groomerloc;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.juanrajc.groomerloc.clasesBD.Cliente;

import java.util.regex.Pattern;

public class RegistroActivity extends AppCompatActivity {

    private RadioGroup rgRegistro;
    private RadioButton rbCliente, rbPeluquero;
    private EditText etRegEmail, etRegPw, etRegPw2, etRegNombre, etRegTlfn;

    private Button botonSiguiente;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

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

        //Instancia del grupo de botones de radio.
        rgRegistro=(RadioGroup) findViewById(R.id.rgRegistro);

        //Listener para el grupo de botones de radio que se encarga de cambiar el texto del botón para seguir dependiendo del rol seleccionado.
        rgRegistro.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                //Dependiendo del rol, muestra un texto u otro.
                if(i==R.id.rbCliente){
                    botonSiguiente.setText(R.string.botonRegistrar);
                }else if(i==R.id.rbPeluquero){
                    botonSiguiente.setText(R.string.botonSiguiente);
                }

            }
        });

        //Instancias de la autenticación y la base de datos de Firebase.
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Si se vuelve a la activity, se reactivan los botones de la misma.
        botonSiguiente.setEnabled(true);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1){
            finish();
        }

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

            //Si el rol seleccionado es el de cliente...
            if(rbCliente.isChecked()){

                //crea el usuario cliente con su email y contraseña...
                auth.createUserWithEmailAndPassword(etRegEmail.getText().toString(), etRegPw.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        //cuando se ha creado, añade el nombre introducido a la cuenta creada (y loqueada)...
                        auth.getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().
                                setDisplayName(etRegNombre.getText().toString()).build());

                        /*
                        crea en la base de datos una colección de clientes (si aún no existe) y un registro (documento)
                        con la id del cliente registrado. Se añaden también sus datos de registro mediante un POJO...
                        */
                        firestore.collection("clientes").document(auth.getCurrentUser()
                                .getUid()).set(new Cliente(Integer.parseInt(etRegTlfn.getText().toString())));

                        //finalmente se cierra la activity.
                        finish();

                    }
                });

            }else if(rbPeluquero.isChecked()){

                Intent intentPeluquero = new Intent(this, RegistroLocActivity.class);

                intentPeluquero.putExtra("email", etRegEmail.getText().toString());
                intentPeluquero.putExtra("pw", etRegPw.getText().toString());
                intentPeluquero.putExtra("nombre", etRegNombre.getText().toString());
                intentPeluquero.putExtra("telefono", Integer.parseInt(etRegTlfn.getText().toString()));

                startActivityForResult(intentPeluquero, 1);

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
