package com.juanrajc.groomerloc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditTarifasPeluActivity extends AppCompatActivity {

    private EditText etEdTaBanio, etEdTaBanioExtra, etEdTaArreglo, etEdTaArregloExtra,
            etEdTaCompleto, etEdTaCompletoExtra, etEdTaDeslanado, etEdTaDeslanadoExtra,
            etEdTaTinte, etEdTaTinteExtra, etEdTaPeso, etEdTaOidos, etEdTaUnias, etEdTaAnales;

    //Objetos de Firebase (Autenticación y BD Firestore).
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tarifas_pelu);

        etEdTaBanio = findViewById(R.id.etEdTaBanio);
        etEdTaBanioExtra = findViewById(R.id.etEdTaBanioExtra);
        etEdTaArreglo = findViewById(R.id.etEdTaArreglo);
        etEdTaArregloExtra = findViewById(R.id.etEdTaArregloExtra);
        etEdTaCompleto = findViewById(R.id.etEdTaCompleto);
        etEdTaCompletoExtra = findViewById(R.id.etEdTaCompletoExtra);
        etEdTaDeslanado = findViewById(R.id.etEdTaDeslanado);
        etEdTaDeslanadoExtra = findViewById(R.id.etEdTaDeslanadoExtra);
        etEdTaTinte = findViewById(R.id.etEdTaTinte);
        etEdTaTinteExtra = findViewById(R.id.etEdTaTinteExtra);
        etEdTaPeso = findViewById(R.id.etEdTaPeso);
        etEdTaOidos = findViewById(R.id.etEdTaOidos);
        etEdTaUnias = findViewById(R.id.etEdTaUnias);
        etEdTaAnales = findViewById(R.id.etEdTaAnales);

        //Instancias de la autenticación y la base de datos de Firebase.
        auth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();
    }

    /**
     * Método que controla el retroceso a la activity anterior.
     *
     * @param view
     */
    protected void atras (View view){

        //Finaliza la activity.
        finish();

    }

    protected void guardaTarifas (View view){



    }

}
