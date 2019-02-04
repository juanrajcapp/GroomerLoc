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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.juanrajc.groomerloc.adaptadores.AdaptadorCitasCliente;
import com.juanrajc.groomerloc.clasesBD.Cita;

import java.util.ArrayList;
import java.util.List;

public class CitasClienteActivity extends AppCompatActivity {

    //Objeto del adaptador que muestra las fichas de las citas del cliente.
    private RecyclerView rvCitasCliente;

    //Objeto del usuario actual y de la BD Firestore.
    private FirebaseUser usuario;
    private FirebaseFirestore firestore;

    //Objeto del botón para recargar las citas del cliente y para retroceder.
    private Button botonRecargaCitasCliente, botonAtrasCitasCliente;

    //Objeto del círculo de carga.
    private ProgressBar circuloCargaCitasCliente;

    //Objeto del EditText que aparece cuando no hay citas.
    private TextView tvNoCitasCliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citas_cliente);

        //Instancia del usuario actual y de la base de datos Firestore.
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        //Instancia del botón para recargar citas del cliente y para retroceder.
        botonRecargaCitasCliente = (Button) findViewById(R.id.botonRecargaCitasCliente);
        botonAtrasCitasCliente = (Button) findViewById(R.id.botonAtrasCitasCliente);

        //Instancia del círculo de carga.
        circuloCargaCitasCliente = (ProgressBar) findViewById(R.id.circuloCargaCitasCliente);

        //Instancia del EditText que aparece cuando no hay citas.
        tvNoCitasCliente = (TextView) findViewById(R.id.tvNoCitasCliente);

        //Instancia del RecyclerView de citas del cliente.
        rvCitasCliente = (RecyclerView) findViewById(R.id.rvCitasCliente);

        //Fija el tamaño del rv, que mejorará el rendimento.
        rvCitasCliente.setHasFixedSize(true);

        //Administrador para el LinearLayout.
        rvCitasCliente.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Se oculta el mensaje de la no existencia de citas...
        tvNoCitasCliente.setVisibility(View.INVISIBLE);

        //y se obtienen las citas del cliente.
        obtieneCitasCliente();

    }

    /**
     * Método que obtiene las citas creadas por el cliente actual desde Firebase Firestore.
     */
    private void obtieneCitasCliente(){

        //Se desactivan los botones de la activity para evitar dobles pulsaciones.
        botonRecargaCitasCliente.setEnabled(false);
        botonAtrasCitasCliente.setEnabled(false);

        //Se visibiliza el círculo de carga.
        circuloCargaCitasCliente.setVisibility(View.VISIBLE);

        firestore.collection("citas").whereEqualTo("idCliente", usuario.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                //Si se obtienen resultados satisfactoriamente...
                if(task.isSuccessful()){

                    //comprueba que esos resultados contienen datos.
                    if(task.getResult().isEmpty()){
                        circuloCargaCitasCliente.setVisibility(View.INVISIBLE);
                        tvNoCitasCliente.setVisibility(View.VISIBLE);
                    }else {

                        /*
                        Si contienen datos, se crean dos List, una con las IDs y otra con
                        los objetos de las citas encontrados...
                        */
                        List<String> listaIdsCitas = new ArrayList<String>();
                        List<Cita> listaObjCitas = new ArrayList<Cita>();

                        //y se introducen dichos datos en dichos List.
                        for (QueryDocumentSnapshot doc : task.getResult()) {

                            listaIdsCitas.add(doc.getId());
                            listaObjCitas.add(doc.toObject(Cita.class));

                        }

                        //Crea un nuevo adaptador con los perros obtenidos.
                        rvCitasCliente.setAdapter(new AdaptadorCitasCliente(listaIdsCitas, listaObjCitas));

                        //Finalizada la carga, se vuelve a invisibilizar el círculo de carga.
                        circuloCargaCitasCliente.setVisibility(View.INVISIBLE);

                    }

                        /*
                        Si ha habido algún problema al obtener resultados,
                        se invisibiliza el círculo de carga y se muestra un toast avisándolo.
                        */
                }else{
                    circuloCargaCitasCliente.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), getString(R.string.mensajeNoResultCitas), Toast.LENGTH_SHORT).show();
                }

                botonRecargaCitasCliente.setEnabled(true);
                botonAtrasCitasCliente.setEnabled(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                circuloCargaCitasCliente.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), getString(R.string.mensajeNoResultCitas), Toast.LENGTH_SHORT).show();
                botonRecargaCitasCliente.setEnabled(true);
                botonAtrasCitasCliente.setEnabled(true);
            }
        });

    }

    /**
     * Método que recarga las citas creadas por el cliente.
     *
     * @param view
     */
    protected void recargaCitasCliente(View view){

        obtieneCitasCliente();

    }

    /**
     * Método que controla el retroceso a la activity anterior.
     *
     * @param view
     */
    protected void atras(View view){

        //Se desactivan los botones para evitar más de una pulsación...
        botonAtrasCitasCliente.setEnabled(false);
        botonRecargaCitasCliente.setEnabled(false);

        //y finaliza la activity.
        finish();

    }

}
