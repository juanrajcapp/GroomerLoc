package com.juanrajc.groomerloc;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class PeluqueroActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    //Objeto del adaptador que muestra las citas concertadas.
    private RecyclerView rvCitas;

    //Objetos de Firebase (Autenticación y BD Firestore).
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    //Objeto del panel lateral (menú).
    private DrawerLayout dw;

    //Objeto del círculo de carga.
    private ProgressBar circuloCargaCitas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peluquero);

        //Instancias de la autenticación y la base de datos de Firebase.
        auth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();

        //Muestra y habilita la pulsación del icono "Home" del ActionBar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //Activa la visibilidad del logo en el ActionBar.
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        //Muestra el nombre del cliente y el logo de la aplicación en el action bar.
        getSupportActionBar().setTitle(auth.getCurrentUser().getDisplayName());
        getSupportActionBar().setLogo(R.mipmap.logo_groomerloc);

        //Codigo del panel lateral.
        dw = (DrawerLayout) findViewById(R.id.drawer_layout_peluquero);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, dw, R.string.abrir_navegacion_lateral, R.string.cerrar_navegacion_lateral);
        dw.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_peluquero);
        navigationView.setNavigationItemSelectedListener(this);

        //Instancia del círculo de carga.
        circuloCargaCitas = (ProgressBar) findViewById(R.id.circuloCargaCitas);

        //Instancia del RecyclerView de citas.
        rvCitas = (RecyclerView) findViewById(R.id.rvCitas);

        //Fija el tamaño del rv, que mejorará el rendimento.
        rvCitas.setHasFixedSize(true);

        //Administrador para el LinearLayout.
        rvCitas.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Si al volver a la activity existe un usuario autenticado...
        if(auth.getCurrentUser()!=null) {

            //se actualiza el nombre del peluquero en el ActionBar.
            getSupportActionBar().setTitle(auth.getCurrentUser().getDisplayName());

            //Si no...
        }else{

            //se cierra la activity.
            finish();

        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        //Según el elemento del panel lateral pulsado...
        switch (item.getItemId()){

            case R.id.nav_tarifas:
                //Inicia la activity que edita las tarifas del peluquero...
                startActivity(new Intent(this, EditTarifasPeluActivity.class));
                //y cierra el menú lateral.
                dw.closeDrawers();
                return true;

            case R.id.nav_citas_confirmadas:
                //Inicia la activity visualiza las citas confirmadas...
                //startActivity(new Intent(this, CitasConfirmadasPeluActivity.class));
                //y cierra el menú lateral.
                dw.closeDrawers();
                return true;

            case R.id.nav_acercaDe:
                //Inicia la activity que muestra información acerca de la aplicación...
                startActivity(new Intent(this, AcercaDeActivity.class));
                //y cierra el menú lateral.
                dw.closeDrawers();
                return true;

            case R.id.nav_preferencias:
                //Inicia la activity de preferencias del peluquero...
                startActivity(new Intent(this, PrefPeluqueroActivity.class));
                //y cierra el menú lateral.
                dw.closeDrawers();
                return true;

            case R.id.nav_cuenta:
                //Inicia la activity que permite editar los datos de la cuenta del peluquero...
                startActivity(new Intent(this, EditCuentaPeluActivity.class));
                //y cierra el menú lateral.
                dw.closeDrawers();
                return true;

            case R.id.nav_log_off:
                //Desloguea al usuario actual...
                auth.signOut();
                //y cierra la activity.
                finish();
                return true;

            case R.id.nav_salir:
                //Cierra la aplicación completamente.
                finishAffinity();
                return true;

            default:
                return false;

        }

    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    /**
     * Método para realizar las acciones del toolbar de la aplicación. Solo realiza la acción de abrir o cerrar el menú lateral.
     * @return
     */
    @Override
    public boolean onSupportNavigateUp() {
        //Si el menú está abierto
        if(dw.isDrawerOpen(Gravity.START)){
            //Cerrará el menú
            dw.closeDrawer(Gravity.START);
        }else{
            //Abrirá el menu
            dw.openDrawer(Gravity.START);
        }
        return super.onSupportNavigateUp();
    }

}
