package com.juanrajc.groomerloc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.juanrajc.groomerloc.adaptadores.AdaptadorBusqPelu;

public class BusqPeluActivity extends AppCompatActivity {

    //Objeto del adaptador que muestra las fichas de los peluqueros.
    private RecyclerView rvBusqPelu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busq_pelu);

        //Instancia del RecyclerView de perros.
        rvBusqPelu = (RecyclerView) findViewById(R.id.rvBusqPelu);

        //Fija el tamaño del rv, que mejorará el rendimento.
        rvBusqPelu.setHasFixedSize(true);

        //Administrador para el LinearLayout.
        rvBusqPelu.setLayoutManager(new LinearLayoutManager(this));

        //Bundle con los datos recibidos desde la activity anterior.
        Bundle datosPeluqueros = getIntent().getExtras();

        //Crea un nuevo adaptador y se le pasan los datos obtenidos de la activity anterior.
        rvBusqPelu.setAdapter(new AdaptadorBusqPelu(datosPeluqueros.getStringArrayList("listaIdPeluqueros"),
                datosPeluqueros.getStringArrayList("listaNombresPeluqueros"),
                datosPeluqueros.getStringArrayList("listaDirPeluqueros")));

    }

    /**
     * Método que devuelve la id del peluquero seleccionado por el usuario a la activity anterior.
     *
     * @param idPeluquero Cadena con la ID que tiene el peluquero en Firebase Firestore.
     */
    public void devuelvePeluqueroSeleccionado(String idPeluquero){

        //Se envía...
        setResult(RESULT_OK, new Intent().putExtra("idPeluquero", idPeluquero));

        //y se cierra la activity.
        finish();

    }

}
