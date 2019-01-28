package com.juanrajc.groomerloc;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
import com.juanrajc.groomerloc.adaptadores.AdaptadorPerrosCita;

import java.util.ArrayList;
import java.util.List;

public class PerrosCitaActivity extends AppCompatActivity {

    //Objeto del adaptador que muestra las fichas de los perros.
    private RecyclerView rvPerrosCita;

    //Objeto del usuario actual y de la BD Firestore.
    private FirebaseUser usuario;
    private FirebaseFirestore firestore;

    //Objeto del círculo de carga.
    private ProgressBar circuloCargaPerrosCita;

    //Objeto del EditText que aparece cuando no hay perros registrados.
    private TextView tvNoPerrosCita;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perros_cita);

        //Instancia del usuario actual y de la base de datos Firestore.
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        //Instancia del círculo de carga.
        circuloCargaPerrosCita = (ProgressBar) findViewById(R.id.circuloCargaPerrosCita);

        //Instancia del EditText que aparece cuando no hay perros registrados.
        tvNoPerrosCita = (TextView) findViewById(R.id.tvNoPerrosCita);

        //Instancia del RecyclerView de perros.
        rvPerrosCita = (RecyclerView) findViewById(R.id.rvPerrosCita);

        //Fija el tamaño del rv, que mejorará el rendimento.
        rvPerrosCita.setHasFixedSize(true);

        //Administrador para el LinearLayout.
        rvPerrosCita.setLayoutManager(new LinearLayoutManager(this));

        obtienePerros();

    }

    /**
     * Método que obtiene los perros registrados por el cliente en Firestore y los muestra en un CardView.
     */
    private void obtienePerros(){

        //Se visibiliza el círculo de carga.
        circuloCargaPerrosCita.setVisibility(View.VISIBLE);

        //Listener que obtiene los perros registrados por el cliente actualmente logueado.
        firestore.collection("clientes").document(usuario.getUid()).collection("perros")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                //Si se obtienen resultados satisfactoriamente...
                if(task.isSuccessful()){

                    //comprueba que esos resultados contienen datos.
                    if(task.getResult().isEmpty()){
                        circuloCargaPerrosCita.setVisibility(View.INVISIBLE);
                        tvNoPerrosCita.setVisibility(View.VISIBLE);
                    }else {

                        //Si contienen datos, se crea un List que contedrá los perros encontrados...
                        List<String> listaPerros = new ArrayList<String>();

                        //y se introducen sus nombres en dicho list uno a uno.
                        for (QueryDocumentSnapshot doc : task.getResult()) {

                            listaPerros.add(doc.getId());

                        }

                        //Crea un nuevo adaptador con los perros obtenidos.
                        rvPerrosCita.setAdapter(new AdaptadorPerrosCita(listaPerros));

                        //Finalizada la carga, se vuelve a invisibilizar el círculo de carga.
                        circuloCargaPerrosCita.setVisibility(View.INVISIBLE);

                    }

                        /*
                        Si ha habido algún problema al obtener resultados,
                        se invisibiliza el círculo de carga y se muestra un toast avisándolo.
                        */
                }else{
                    circuloCargaPerrosCita.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), getString(R.string.mensajeNoResultPerros), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
