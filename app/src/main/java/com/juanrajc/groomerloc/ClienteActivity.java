package com.juanrajc.groomerloc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.juanrajc.groomerloc.clasesBD.Peluquero;

public class ClienteActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    //Objeto del mapa que se muestra en la activity.
    private GoogleMap map;

    //Objetos de Firebase (Autenticación y BD Firestore).
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    //Objeto del panel lateral (menú).
    private DrawerLayout dw;

    //Objeto del campo de texto que va a recoger el nombre de un peluquero para su búsqueda.
    EditText entradaNombrePelu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente);

        //Carga del fragment con el mapa.
        SupportMapFragment mapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
        dw = (DrawerLayout) findViewById(R.id.drawer_layout_cliente);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, dw, R.string.abrir_navegacion_lateral, R.string.cerrar_navegacion_lateral);
        dw.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_cliente);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
       //Botón de retroceso desactivado en esta activity.
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Si se aceptan los permisos de localización...
        if((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {

            //se recarga la activity para que muestre la localización actual del usuario.
            finish();
            startActivity(getIntent());
        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        //Instancia del mapa.
        map=googleMap;

        //Comprobar si tenemos permiso de geolocalización para habilitar el botón de mi ubicación
        if(comprobarPermisoLocalizacion() && comprobarPermisosLocalizacionAproximada()){

            //Si es así, activamos y mostramos la localización del usuario.
            googleMap.setMyLocationEnabled(true);

            //Listener que se encarga de mostrar la localización GPS actual.
            LocationServices.getFusedLocationProviderClient(this).getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location!=null) {

                        //Si se muestra con éxito, se hace zoom sobre dicha localización...
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));

                        //y ejecuta el método que muestra los peluqueros de alrededor,
                        marcaPeluqueros();

                    }
                }
            });
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        //Según el elemento del panel lateral pulsado...
        switch (item.getItemId()){

            case R.id.nav_buscPelu:
                //Ejecuta el método de búsqueda de peluqueros...
                dialogBuscaPeluqueros();
                //y cierra el menú lateral.
                dw.closeDrawers();
                return true;

            case R.id.nav_perro:
                //Inicia la activity de perros...
                startActivity(new Intent(this, PerrosActivity.class));
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

    /**
     * Método que comprueba si tenemos persmiso de localización sobre el dispositivo
     *
     * @return Verdadero (Con permiso) o falso (Sin permiso).
     */
    private boolean comprobarPermisoLocalizacion(){
        //Comprobar si tenemos persmisos sobre localización.
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
            // Solicitar permiso en caso de que no lo disponga.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return false;
        }
        return true;
    }

    /**
     * Método que comprueba si tenemos persmiso de localización aproximada sobre el dispositivo
     *
     * @return Verdadero (Con permiso) o falso (Sin permiso).
     */
    private boolean comprobarPermisosLocalizacionAproximada(){
        //Comprobar si tenemos persmisos sobre localización.
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED){
            // Solicitar permiso en caso de que no lo disponga.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
            return false;
        }
        return true;
    }

    /**
     * Método que marca en mapa los peluqueros existentes en la base de datos.
     */
    private void marcaPeluqueros(){

        //Listener que obtiene los peluqueros existentes en la base de datos.
        firestore.collection("peluqueros").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                //Si se ha completado la búsqueda con éxito...
                if(task.isSuccessful()){

                    //por cada peluquero obtenido...
                    for(DocumentSnapshot doc : task.getResult()){

                        //marca en el mapa su posición con su nombre.
                        marcarMapa(new LatLng(doc.toObject(Peluquero.class).getLoc().getLatitude()
                                , doc.toObject(Peluquero.class).getLoc().getLongitude()), doc.toObject(Peluquero.class).getNombre());

                    }
                }
            }
        });
    }

    /**
     * Método que crea el dialog donde se va a introducir el nombre para la búsqueda de peluqueros.
     */
    private void dialogBuscaPeluqueros(){

        //Instancia del EditText que recogerá el nombre del peluquero a buscar.
        entradaNombrePelu = new EditText(this);

        AlertDialog.Builder adBuscaPelu = new AlertDialog.Builder(this);
        adBuscaPelu.setMessage(getString(R.string.tituloADBuscadorPelu));

        //Añade el EditText al AlertDialog.
        adBuscaPelu.setView(entradaNombrePelu);

        //Setea los botones del AlertDialog y se muestra.
        adBuscaPelu.setPositiveButton(getString(R.string.botonBuscar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                buscaPeluqueros(entradaNombrePelu.getText().toString().toLowerCase());
            }
        }).setNegativeButton(getString(R.string.salir), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Cierra el dialog.
                dialogInterface.dismiss();
            }
        }).show();

    }

    /**
     * Método que busca el o los peluqueros existentes en Firestore con el nombre pasado por parámetro.
     *
     * @param nombre Cadena con el nombre del peluquero que se desea buscar.
     */
    private void buscaPeluqueros(String nombre){

        //Busca en los arrays de nombres de los peluqueros una coincidencia exacta...
        firestore.collection("peluqueros").whereArrayContains("nombresBusqueda",nombre)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                //si el resultado de la búsqueda es satisfactorio...
                if(task.isSuccessful()){

                    //guarda el resultado.
                    QuerySnapshot respuesta = task.getResult();

                    //Comprueba si el resultado contiene datos.
                    if(respuesta.isEmpty()){
                        Toast.makeText(getApplicationContext(), getString(R.string.mensajeBusqSinRes), Toast.LENGTH_SHORT).show();
                    }else{

                        //Si la respuesta solo ha dado un resultado...
                        if(respuesta.size()<=1){

                            //muestra directamente la ubicación del peluquero devuelto.
                            for(QueryDocumentSnapshot doc:respuesta){

                                Peluquero peluquero = doc.toObject(Peluquero.class);

                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(peluquero.getLoc().getLatitude()
                                                , peluquero.getLoc().getLongitude()), 15));

                            }

                        //Si no...
                        }else{

                            Toast.makeText(getApplicationContext(), "MUCHOS", Toast.LENGTH_SHORT).show();

                        }

                    }

                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.mensajeBusqNoComp), Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.mensajeBusqError), Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Método que se encarga de generar una marca con nombre en el mapa.
     *
     * @param coordenadas LatLng con las coordenadas donde se va a generar la marca.
     * @param peluquero Cadena con el nombre del peluquero.
     */
    private void marcarMapa (LatLng coordenadas, String peluquero){

        //Se crea una marca.
        MarkerOptions mo=new MarkerOptions();
        //Se le pasa la posición seleccionada.
        mo.position(coordenadas);
        //Se le añade un título a la marca.
        mo.title(peluquero);
        //Se posiciona la marca en el lugar seleccionado.
        map.addMarker(mo);

    }
}
