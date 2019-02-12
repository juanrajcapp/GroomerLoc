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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.juanrajc.groomerloc.adaptadores.AdaptadorCitasPelu;
import com.juanrajc.groomerloc.clasesBD.Cita;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PeluqueroActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    //Objeto del adaptador que muestra las citas concertadas.
    private RecyclerView rvCitas;

    //Objetos de Firebase (Autenticación y BD Firestore).
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    //Objeto del botón para recargar las citas.
    private Button botonRecargarCitas;

    //Objeto del panel lateral (menú).
    private DrawerLayout dw;

    //Objeto del círculo de carga.
    private ProgressBar circuloCargaCitas;

    //Objeto del EditText que aparece cuando no hay citas.
    private TextView tvNoCitas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peluquero);

        //Instancias de la autenticación y la base de datos de Firebase.
        auth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();

        //Instancia del botón para recargar citas.
        botonRecargarCitas = (Button) findViewById(R.id.botonRecargarCitas);

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

        //Instancia del círculo de carga y mensaje de no existencia de citas.
        circuloCargaCitas = (ProgressBar) findViewById(R.id.circuloCargaCitas);
        tvNoCitas = (TextView) findViewById(R.id.tvNoCitas);

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

            //se actualiza el nombre del peluquero en el ActionBar...
            getSupportActionBar().setTitle(auth.getCurrentUser().getDisplayName());

            //se oculta el mensaje de la no existencia de citas...
            tvNoCitas.setVisibility(View.INVISIBLE);

            //y se obtienen las citas.
            obtieneCitas();

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
                //Inicia la activity que visualiza las citas confirmadas...
                startActivity(new Intent(this, CitasConfPeluActivity.class));
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
     *
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

    /**
     * Método que obtiene las citas solicitadas sin fecha establecida.
     */
    private void obtieneCitas(){

        //Se desactivan los botones de la activity para evitar dobles pulsaciones.
        botonRecargarCitas.setEnabled(false);

        //Se visibiliza el círculo de carga.
        circuloCargaCitas.setVisibility(View.VISIBLE);

        //Obtiene las citas credas por el peluquero actual en orden de fecha de creación descendente desde Firestore.
        firestore.collection("citas").whereEqualTo("idPeluquero", auth.getCurrentUser().getUid())
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                //Si se obtienen resultados satisfactoriamente...
                if(task.isSuccessful()){

                    //comprueba que esos resultados contienen datos.
                    if(task.getResult().isEmpty()){
                        rvCitas.setAdapter(new AdaptadorCitasPelu(null, null));
                        tvNoCitas.setVisibility(View.VISIBLE);
                    }else {

                        /*
                        Si contienen datos, se crean dos List, una con las IDs y otra con
                        los objetos de las citas encontrados...
                        */
                        List<String> listaIdsCitas = new ArrayList<String>();
                        List<Cita> listaObjCitas = new ArrayList<Cita>();

                        //y se introducen dichos datos en dichos List...
                        for (QueryDocumentSnapshot doc : task.getResult()) {

                            //comprobando antes la vigencia de cada cita.
                            if(compruebaVigenciaCita(doc.toObject(Cita.class))) {

                                //Sólo se van a mostrar los que no tienen aún fecha de confirmación.
                                if(doc.toObject(Cita.class).getFechaConfirmacion()==null) {

                                    listaIdsCitas.add(doc.getId());
                                    listaObjCitas.add(doc.toObject(Cita.class));

                                }

                            }else{

                                borraCita(doc.getId(), doc.toObject(Cita.class));

                            }

                        }

                        /*
                        Si no se ha añadido ninguna cita al list, se muestra un mensaje en
                        la vista de la activity.
                        */
                        if(listaIdsCitas.isEmpty()) {
                            tvNoCitas.setVisibility(View.VISIBLE);
                        }

                        //Crea un nuevo adaptador con las citas obtenidas.
                        rvCitas.setAdapter(new AdaptadorCitasPelu(listaIdsCitas, listaObjCitas));

                    }

                //Si ha habido algún problema al obtener resultados, se muestra un toast avisándolo.
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.mensajeNoResultCitas),
                            Toast.LENGTH_SHORT).show();
                }

                //Finalizada la carga, se vuelve a invisibilizar el círculo de carga y a activar los botones.
                circuloCargaCitas.setVisibility(View.INVISIBLE);
                botonRecargarCitas.setEnabled(true);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                circuloCargaCitas.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), getString(R.string.mensajeNoResultCitas),
                        Toast.LENGTH_SHORT).show();
                botonRecargarCitas.setEnabled(true);
            }
        });

    }

    /**
     * Método que comprueba si la cita es válida en el tiempo (que no ha pasado más de una semana
     * desde que se creó) y que no tenga aún ninguna fecha confirmada para la realización del servicio.
     *
     * @param cita Objeto de la cita, el cual contiene sus datos.
     *
     * @return Booleano true si la cita está vigente.
     */
    private boolean compruebaVigenciaCita(Cita cita){

        if(cita.getFechaConfirmacion()==null){
            return compruebaVigenciaCitaNoConfirmada(cita.getFechaCreacion());
        }else{
            return compruebaVigenciaCitaConfirmada(cita.getFechaConfirmacion());
        }

    }

    /**
     * Método que comprueba si una cita sin fecha de confirmación ha sido creada hace menos de una semana.
     *
     * @param fechaCreacion Date con la fecha de creación de la cita.
     *
     * @return Booleano true si la cita ha sido creada hace menos de una semana.
     */
    private boolean compruebaVigenciaCitaNoConfirmada(Date fechaCreacion){

        //Obtiene los días pasados entre la fecha de creación y la fecha actual.
        long dias = TimeUnit.DAYS.convert(Calendar.getInstance().getTime().getTime()
                - fechaCreacion.getTime(), TimeUnit.MILLISECONDS);

        //Comprueba si han pasado más de 7 días.
        if(dias>7){
            return false;
        }else{
            return true;
        }

    }

    /**
     * Método que comprueba si ha pasado menos de 6 horas desde que expiró la fecha confirmada de
     * realización del servicio de una cita (o sigue vigente).
     *
     * @param fechaConfirmacion Date con la fecha confirmada para la realización del servicio de la cita.
     *
     * @return Booleano true si han pasado menos de 6 horas desde la realización del servicio de la cita
     * (o sigue vigente).
     */
    private boolean compruebaVigenciaCitaConfirmada(Date fechaConfirmacion){

        //Obtiene las horas pasadas entre la fecha de confirmación y la fecha actual.
        long horas = TimeUnit.HOURS.convert(Calendar.getInstance().getTime().getTime()
                - fechaConfirmacion.getTime(), TimeUnit.MILLISECONDS);

        //Comprueba si han pasado más de 6 horas.
        if(horas>6){
            return false;
        }else{
            return true;
        }

    }

    /**
     * Método que se encarga de borrar la cita recibida en Firestore, y su respectiva foto en Storage
     * (si existe).
     *
     * @param idCita Cadena con la ID de la cita a borrar.
     */
    private void borraCita(final String idCita, final Cita cita){

        //Borra la cita recibida mediante su ID en Firestore.
        firestore.collection("citas").document(idCita).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            //Si se ha borrado la cita con éxito, se borra también el chat (si existe)...
                            borraChat(idCita);

                            //y los ficheros de la cita en Storage.
                            FirebaseStorage.getInstance().getReference("citas/"+idCita +"/perros/"
                                    +cita.getPerro().getNombre()+" "+cita.getPerro().getFechaFoto()+".jpg")
                                    .delete();

                        }
                    }
                });

    }

    /**
     * Método que borra el chat de la cita (si existe).
     *
     * @param idCita Cadena con la ID de la cita.
     */
    private void borraChat(final String idCita){

        //Obtiene todos los mensajes del chat de la cita en Firestore.
        firestore.collection("citas").document(idCita).collection("chat")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){

                    //Si el chat no está vacío...
                    if(!task.getResult().isEmpty()){

                        //borra uno por uno dichos mensajes.
                        for(QueryDocumentSnapshot doc:task.getResult()){

                            firestore.collection("citas").document(idCita)
                                    .collection("chat").document(doc.getId())
                                    .delete();

                        }

                    }

                }
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

}
