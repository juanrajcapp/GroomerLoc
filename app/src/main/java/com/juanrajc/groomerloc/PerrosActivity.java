package com.juanrajc.groomerloc;

import android.net.Uri;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.juanrajc.groomerloc.adaptadores.AdaptadorPerros;
import com.juanrajc.groomerloc.recursos.CardPerro;

import java.util.ArrayList;
import java.util.List;

public class PerrosActivity extends AppCompatActivity {

    private RecyclerView rvPerros;

    //Objeto del usuario actual, de la BD Firestore y de la referencia al almacenamiento de ficheros Storage.
    private FirebaseUser usuario;
    private FirebaseFirestore firestore;
    private StorageReference refFotoPerro;

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

    private void obtienePerros(){

        refFotoPerro = FirebaseStorage.getInstance().getReference();

        firestore.collection("clientes").document(usuario.getUid()).collection("perros")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){

                            List<CardPerro> listaPerros;

                            if(task.getResult().isEmpty()){
                                Toast.makeText(getApplicationContext(), getString(R.string.mensajeNoPerros), Toast.LENGTH_SHORT).show();
                            }else {

                                listaPerros = new ArrayList<CardPerro>();

                                for (QueryDocumentSnapshot doc : task.getResult()) {

                                    listaPerros.add(new CardPerro(doc.getId(), Uri.parse("gs://groomerloc.appspot.com/fotos/nxIqzMFY9mf1qjsp1nWSBiWIDYE3/Jara.jpg")));

                                }

                                //Crea un nuevo adaptador.
                                rvPerros.setAdapter(new AdaptadorPerros(listaPerros));

                            }

                        }else{
                            Toast.makeText(getApplicationContext(), getString(R.string.mensajeNoResultPerros), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
