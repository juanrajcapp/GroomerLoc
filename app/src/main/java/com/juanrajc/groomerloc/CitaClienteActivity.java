package com.juanrajc.groomerloc;

import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.juanrajc.groomerloc.adaptadores.AdaptadorPagCitaCli;

public class CitaClienteActivity extends AppCompatActivity {

    //Objetos del Tab de la activity.
    private TabLayout tabLayCitaCliente;
    private TabItem tabItemCitaClienteDatos, tabItemCitaClienteChat;
    private ViewPager vpCitaCliente;

    //Objeto del adaptador que controla el cambio de página (fragment).
    private PagerAdapter adPagCitaCliente;

    private String idCita;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cita_cliente);

        //Instancia de los elementos de la vista de la activity.
        tabLayCitaCliente = findViewById(R.id.tabLayCitaCliente);
        tabItemCitaClienteDatos = findViewById(R.id.tabItemCitaClienteDatos);
        tabItemCitaClienteChat = findViewById(R.id.tabItemCitaClienteChat);
        vpCitaCliente = findViewById(R.id.vpCitaCliente);

        //Instancia y asocia el adaptador para el manejo del cambio de página (fragment).
        adPagCitaCliente = new AdaptadorPagCitaCli(getSupportFragmentManager(), tabLayCitaCliente.getTabCount());
        //Asocia el adaptador al ViewPager de la activity.
        vpCitaCliente.setAdapter(adPagCitaCliente);
        //Listener que asocia el cambio de página (fragment) a los Tabs de la vista.
        vpCitaCliente.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayCitaCliente));
        //Listener que asocia la pulsación en los Tabs al cambio de página (fragment).
        tabLayCitaCliente.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                vpCitaCliente.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        idCita = getIntent().getStringExtra("idCita");

    }

    public String getIdCita(){
        return idCita;
    }

}
