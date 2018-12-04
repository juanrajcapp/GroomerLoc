package com.juanrajc.groomerloc;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perros);

        //Instancia del usuario actual y de la base de datos Firestore.
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        //Instancia el RecyclerView de perros.
        rvPerros = (RecyclerView) findViewById(R.id.rvPerros);

        //Fija el tamaño del rv, que mejorará el rendimento.
        rvPerros.setHasFixedSize(true);

        //Administrador para el LinearLayout.
        rvPerros.setLayoutManager(new LinearLayoutManager(this));

        obtienePerros();

    }

    /**
     * Método que obtiene los perros registrados por el cliente en Firestore y los muestra en un CardView.
     */
    private void obtienePerros(){

        //Listener que obtiene los perros registrados por el cliente actualmente logueado.
        firestore.collection("clientes").document(usuario.getUid()).collection("perros")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        //Si se obtienen resultados satisfactoriamente...
                        if(task.isSuccessful()){

                            //comprueba que esos resultados contienen datos.
                            if(task.getResult().isEmpty()){
                                Toast.makeText(getApplicationContext(), getString(R.string.mensajeNoPerros), Toast.LENGTH_SHORT).show();
                            }else {

                                //Si contienen datos, se crea un List que contedrá los perros encontrados...
                                List<String> listaPerros = new ArrayList<String>();

                                //y se introducen sus nombres en dicho list uno a uno.
                                for (QueryDocumentSnapshot doc : task.getResult()) {

                                    listaPerros.add(doc.getId());

                                }

                                //Crea un nuevo adaptador con los perros obtenidos.
                                rvPerros.setAdapter(new AdaptadorPerros(listaPerros));

                            }

                        //Si ha habido algún problema al obtener resultados, se muestra un toast avisándolo.
                        }else{
                            Toast.makeText(getApplicationContext(), getString(R.string.mensajeNoResultPerros), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
