package com.juanrajc.groomerloc;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegPerroActivity extends AppCompatActivity {

    //Objetos de la vista de la activity.
    private RadioGroup grupoSexo;
    private RadioButton perroMacho, perroHembra;
    private EditText nombrePerro, razaPerro, pesoPerro, comentPerro;

    //Objeto del botón registro de perro de la vista.
    private Button botonRegPerro;

    //Objeto del usuario actual.
    private FirebaseUser usuario;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_perro);

        //Instancia del usuario actual y de la base de datos Firestore.
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        //Instancia del grupo de botones de radio.
        grupoSexo = (RadioGroup) findViewById(R.id.rgRegSexo);

        //Instancia de los botones de radio para seleccionar el sexo de la mascota.
        perroMacho = (RadioButton) findViewById(R.id.rbRegMacho);
        perroHembra = (RadioButton) findViewById(R.id.rbRegHembra);

        //Instancia de los campos de registro de la vista.
        nombrePerro = (EditText) findViewById(R.id.etRegNombrePerro);
        razaPerro = (EditText) findViewById(R.id.etRegRaza);
        pesoPerro = (EditText) findViewById(R.id.etRegPeso);
        comentPerro = (EditText) findViewById(R.id.etRegComent);

        //Instancia del botón de registro del perro de la vista.
        botonRegPerro = (Button) findViewById(R.id.botonRegPerro);

    }

    /**
     * Método que muestra el diálogo que permite seleccionar el método de obtención de imagen.
     *
     * @param view
     */
    protected void dialogoImagen(View view){

        //Array con las opciones mostradas en el dialog.
        final CharSequence[] opciones = {"Cámara", "Galería", "Cancelar"};

        //Instancia del dialog con el estilo definido en "styles".
        AlertDialog.Builder ad = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);

        //Crea un TV para personalizar el título del dialog.
        TextView tituloDialog = new TextView(this);
        tituloDialog.setText("¿Qué desea utilizar?");
        tituloDialog.setGravity(Gravity.CENTER);
        tituloDialog.setBackgroundColor(Color.GRAY);
        tituloDialog.setTextColor(Color.WHITE);
        tituloDialog.setPadding(8, 8, 8, 8);
        tituloDialog.setTextSize(20);

        //Añade el TV al dialog.
        ad.setCustomTitle(tituloDialog);

        //Listener que controla cual es la opción seleccionada en el dialog.
        ad.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (opciones[i].equals("Cámara")) {

                } else if (opciones[i].equals("Galería")) {

                } else if (opciones[i].equals("Cancelar")) {
                    dialogInterface.dismiss();
                }

            }
        });

        //Muestra el dialog.
        ad.show();

    }

    /**
     * Método que controla la validez de los campos del formulario de la activity de registro de mascotas.
     *
     * @return Devuelve true si los campos son válidos, o false si no lo son.
     */
    private boolean compruebaCampos(){

        //Comprueba que los campos están correctamente completados.
        if(nombrePerro.getText().toString().length()>0 && razaPerro.getText().toString().length()>0
                && pesoPerro.getText().toString().length()>0){

            //Comprueba que se ha seleccionado uno de los dos sexos.
            if(perroMacho.isChecked() || perroHembra.isChecked()){

                //Devuelve verdadero cuando el formulario está correcto.
                return true;

            } else {
                Toast.makeText(this, getString(R.string.mensajeSexo), Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, getString(R.string.mensajeCamposPerro), Toast.LENGTH_SHORT).show();
        }

        return false;

    }

    protected  void regPerro (View view){

        if(compruebaCampos()){

            botonRegPerro.setEnabled(false);

        }

    }

    protected void atras (View view){

        finish();

    }

}
