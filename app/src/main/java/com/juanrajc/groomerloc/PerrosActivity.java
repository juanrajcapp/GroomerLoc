package com.juanrajc.groomerloc;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.juanrajc.groomerloc.adaptadores.AdaptadorPerros;

import java.util.ArrayList;
import java.util.List;

public class PerrosActivity extends AppCompatActivity {

    //Objeto del adaptador que muestra las fichas de los perros.
    private RecyclerView rvPerros;

    //Objeto del usuario actual y de la BD Firestore.
    private FirebaseUser usuario;
    private FirebaseFirestore firestore;

    //Objeto del botón para registrar nuevo perro.
    private Button botonActRegPerro;

    //Objeto del círculo de carga.
    private ProgressBar circuloCargaPerros;

    //Objeto del EditText que aparece cuando no hay perros registrados.
    private TextView tvNoPerros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perros);

        //Instancia del usuario actual y de la base de datos Firestore.
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        //Instancia del botón para registrar nuevo perro.
        botonActRegPerro = (Button) findViewById(R.id.botonActRegPerro);

        //Instancia del círculo de carga.
        circuloCargaPerros = (ProgressBar) findViewById(R.id.circuloCargaPerros);

        //Instancia del EditText que aparece cuando no hay perros registrados.
        tvNoPerros = (TextView) findViewById(R.id.tvNoPerros);

        //Instancia del RecyclerView de perros.
        rvPerros = (RecyclerView) findViewById(R.id.rvPerros);

        //Fija el tamaño del rv, que mejorará el rendimento.
        rvPerros.setHasFixedSize(true);

        //Administrador para el LinearLayout.
        rvPerros.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Si se vuelve a la activity, se reactivan los botones de la misma...
        botonActRegPerro.setEnabled(true);

        //Y se oculta el mensaje de la no existencia de perros registrados.
        tvNoPerros.setVisibility(View.INVISIBLE);

        //Se obtienen los perros registrados.
        obtienePerros();

    }

    /**
     * Método que inicia la activity de registro de perro al pulsar su respectivo botón.
     *
     * @param view
     */
    protected void nuevoPerro(View view){

        //Se desactiva el botón para evitar más de una pulsación.
        botonActRegPerro.setEnabled(false);

        //Inicia la activity que se encarga del registro de un nuevo perro.
        startActivity(new Intent(this, RegPerroActivity.class));

    }

    /**
     * Método que obtiene los perros registrados por el cliente en Firestore y los muestra en un CardView.
     */
    private void obtienePerros(){

        //Se visibiliza el círculo de carga.
        circuloCargaPerros.setVisibility(View.VISIBLE);

        //Listener que obtiene los perros registrados por el cliente actualmente logueado.
        firestore.collection("clientes").document(usuario.getUid()).collection("perros")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        //Si se obtienen resultados satisfactoriamente...
                        if(task.isSuccessful()){

                            //comprueba que esos resultados contienen datos.
                            if(task.getResult().isEmpty()){
                                circuloCargaPerros.setVisibility(View.INVISIBLE);
                                tvNoPerros.setVisibility(View.VISIBLE);
                            }else {

                                //Si contienen datos, se crea un List que contedrá los perros encontrados...
                                List<String> listaPerros = new ArrayList<String>();

                                //y se introducen sus nombres en dicho list uno a uno.
                                for (QueryDocumentSnapshot doc : task.getResult()) {

                                    listaPerros.add(doc.getId());

                                }

                                //Crea un nuevo adaptador con los perros obtenidos.
                                rvPerros.setAdapter(new AdaptadorPerros(listaPerros));

                                //Finalizada la carga, se vuelve a invisibilizar el círculo de carga.
                                circuloCargaPerros.setVisibility(View.INVISIBLE);

                            }

                        /*
                        Si ha habido algún problema al obtener resultados,
                        se invisibiliza el círculo de carga y se muestra un toast avisándolo.
                        */
                        }else{
                            circuloCargaPerros.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), getString(R.string.mensajeNoResultPerros), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
