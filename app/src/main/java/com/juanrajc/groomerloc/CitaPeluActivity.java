package com.juanrajc.groomerloc;

import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.juanrajc.groomerloc.adaptadores.AdaptadorPagCitaPelu;

public class CitaPeluActivity extends AppCompatActivity {

    //Objetos del Tab de la activity.
    private TabLayout tabLayCitaPelu;
    private TabItem tabItemCitaPeluDatos, tabItemCitaPeluChat;
    private ViewPager vpCitaPelu;

    //Objeto del adaptador que controla el cambio de página (fragment).
    private PagerAdapter adPagCitaPelu;

    private String idCita;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cita_pelu);

        //Instancia de los elementos de la vista de la activity.
        tabLayCitaPelu = findViewById(R.id.tabLayCitaPelu);
        tabItemCitaPeluDatos = findViewById(R.id.tabItemCitaPeluDatos);
        tabItemCitaPeluChat = findViewById(R.id.tabItemCitaPeluChat);
        vpCitaPelu = findViewById(R.id.vpCitaPelu);

        //Instancia y asocia el adaptador para el manejo del cambio de página (fragment).
        adPagCitaPelu = new AdaptadorPagCitaPelu(getSupportFragmentManager(), tabLayCitaPelu.getTabCount());
        //Asocia el adaptador al ViewPager de la activity.
        vpCitaPelu.setAdapter(adPagCitaPelu);
        //Listener que asocia el cambio de página (fragment) a los Tabs de la vista.
        vpCitaPelu.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayCitaPelu));
        //Listener que asocia la pulsación en los Tabs al cambio de página (fragment).
        tabLayCitaPelu.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                vpCitaPelu.setCurrentItem(tab.getPosition());

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
