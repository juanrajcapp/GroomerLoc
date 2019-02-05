package com.juanrajc.groomerloc;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.juanrajc.groomerloc.adaptadores.AdaptadorCitasPelu;
import com.juanrajc.groomerloc.clasesBD.Cita;

import java.util.ArrayList;
import java.util.List;

public class CitasConfPeluActivity extends AppCompatActivity {

    //Objeto del adaptador que muestra las citas concertadas.
    private RecyclerView rvCitasConfPelu;

    //Objetos de Firebase (Autenticación y BD Firestore).
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    //Objetos de los botones para recargar las citas y retroceder.
    private Button botonRecargarCitasConfPelu, botonAtrasCitasConfPelu;

    //Objeto del círculo de carga.
    private ProgressBar circuloCargaCitasConfPelu;

    //Objeto del EditText que aparece cuando no hay citas.
    private TextView tvNoCitasConfPelu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citas_conf_pelu);

        //Instancias de la autenticación y la base de datos de Firebase.
        auth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();

        //Instancias de los botones para recargar citas y retroceder.
        botonRecargarCitasConfPelu = (Button) findViewById(R.id.botonRecargarCitasConfPelu);
        botonAtrasCitasConfPelu = (Button) findViewById(R.id.botonAtrasCitasConfPelu);

        //Instancia del círculo de carga y mensaje de no existencia de citas.
        circuloCargaCitasConfPelu = (ProgressBar) findViewById(R.id.circuloCargaCitasConfPelu);
        tvNoCitasConfPelu = (TextView) findViewById(R.id.tvNoCitasConfPelu);

        //Instancia del RecyclerView de citas.
        rvCitasConfPelu = (RecyclerView) findViewById(R.id.rvCitasConfPelu);

        //Fija el tamaño del rv, que mejorará el rendimento.
        rvCitasConfPelu.setHasFixedSize(true);

        //Administrador para el LinearLayout.
        rvCitasConfPelu.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Se oculta el mensaje de la no existencia de citas...
        tvNoCitasConfPelu.setVisibility(View.INVISIBLE);

        //y se obtienen las citas.
        obtieneCitas();

    }

    /**
     * Método que obtiene las citas solicitadas con fecha establecida.
     */
    private void obtieneCitas(){

        //Se desactivan los botones de la activity para evitar dobles pulsaciones.
        botonRecargarCitasConfPelu.setEnabled(false);
        botonAtrasCitasConfPelu.setEnabled(false);

        //Se visibiliza el círculo de carga.
        circuloCargaCitasConfPelu.setVisibility(View.VISIBLE);

        firestore.collection("citas").whereEqualTo("idPeluquero", auth.getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                //Si se obtienen resultados satisfactoriamente...
                if(task.isSuccessful()){

                    //comprueba que esos resultados contienen datos.
                    if(task.getResult().isEmpty()){
                        tvNoCitasConfPelu.setVisibility(View.VISIBLE);
                    }else {

                        /*
                        Si contienen datos, se crean dos List, una con las IDs y otra con
                        los objetos de las citas encontrados...
                        */
                        List<String> listaIdsCitas = new ArrayList<String>();
                        List<Cita> listaObjCitas = new ArrayList<Cita>();

                        //y se introducen dichos datos en dichos List.
                        for (QueryDocumentSnapshot doc : task.getResult()) {

                            //Sólo introduce los que tengan establecida la fecha de confirmación.
                            if(doc.toObject(Cita.class).getFechaConfirmacion()!=null) {

                                listaIdsCitas.add(doc.getId());
                                listaObjCitas.add(doc.toObject(Cita.class));

                            }

                        }

                        //Si se ha introducido algún ID en el list de IDs...
                        if(!listaIdsCitas.isEmpty()) {

                            //crea un nuevo adaptador con las citas obtenidas.
                            rvCitasConfPelu.setAdapter(new AdaptadorCitasPelu(listaIdsCitas, listaObjCitas));

                        //Si no, visibiliza un mensaje en la activity.
                        }else{
                            tvNoCitasConfPelu.setVisibility(View.VISIBLE);
                        }

                    }

                //Si ha habido algún problema al obtener resultados, se muestra un toast avisándolo.
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.mensajeNoResultCitas),
                            Toast.LENGTH_SHORT).show();
                }

                //Finalizada la carga, se vuelve a invisibilizar el círculo de carga y a activar los botones.
                circuloCargaCitasConfPelu.setVisibility(View.INVISIBLE);
                botonRecargarCitasConfPelu.setEnabled(true);
                botonAtrasCitasConfPelu.setEnabled(true);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                circuloCargaCitasConfPelu.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), getString(R.string.mensajeNoResultCitas),
                        Toast.LENGTH_SHORT).show();
                botonRecargarCitasConfPelu.setEnabled(true);
                botonAtrasCitasConfPelu.setEnabled(true);
            }
        });

    }

    /**
     * Método que recarga las citas.
     *
     * @param view
     */
    protected void recargaCitas(View view){

        obtieneCitas();

    }

    /**
     * Método que controla el retroceso a la activity anterior.
     *
     * @param view
     */
    protected void atras(View view){

        //Se desactivan los botones para evitar más de una pulsación...
        botonAtrasCitasConfPelu.setEnabled(false);
        botonRecargarCitasConfPelu.setEnabled(false);

        //y finaliza la activity.
        finish();

    }

}
