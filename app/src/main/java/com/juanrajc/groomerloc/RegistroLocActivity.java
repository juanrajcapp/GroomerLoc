package com.juanrajc.groomerloc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.juanrajc.groomerloc.clasesBD.Peluquero;
import com.juanrajc.groomerloc.recursos.MiLatLng;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistroLocActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Objeto del mapa que se muestra en la activity.
    GoogleMap map;

    //Geocoder para la traducción de coordenadas en direciones y viceversa.
    Geocoder gc;
    //Lista de direcciones obtenidas mediante el Geocoder.
    List<Address> direcciones;

    //Objetos de Firebase (Autenticación y BD Firestore).
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    //Coordenadas de la marca creada por el usuario.
    LatLng loc=null;

    //Datos obtenidos de la activity anterior para el registro del usuario.
    private String email, pw, nombre;
    private long telefono;

    //Objetos de la vista de la activity.
    private EditText etDireccion, etDatAdi;
    private ImageButton botonLoc;
    private Button botonBuscaDir, botonFinReg, botonAtrasRegLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_loc);

        //Instancia de los campos de dirección.
        etDireccion = (EditText) findViewById(R.id.etDireccion);
        etDatAdi = (EditText) findViewById(R.id.etDatAdi);

        //Instancia del botón de registro y atrás.
        botonBuscaDir = (Button) findViewById(R.id.botonBuscaDir);
        botonFinReg = (Button) findViewById(R.id.botonFinRegPel);
        botonAtrasRegLoc = (Button) findViewById(R.id.botonAtrasReg2);
        /*Inician 'no clicables' algunos botones de la activity. No se podrán cliquear hasta
        que los datos se hayan cargado.*/
        activaBotones(false);

        //Botón de localización personalizado con su listener, que se ejecuta cuando se pulsa.
        botonLoc = (ImageButton) findViewById(R.id.botonLoc);
        botonLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                localizacion();
            }
        });
        //Por defecto desactivado.
        botonLoc.setClickable(false);

        //Evento que detecta la pulsación del botón del teclado virtual (búsqueda) y ejecuta el método correspomndiente.
        etDireccion.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    buscaCoordenadas();
                }
                return false;
            }
        });

        //Evento que detecta si hay o no contenido en el EditText de dirección.
        etDireccion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {

                //Si el campo contiene texto, permite el uso del botón de buscar, si no, no.
                if(editable.toString().length()>0){
                    botonBuscaDir.setEnabled(true);
                }else{
                    botonBuscaDir.setEnabled(false);
                }

            }
        });

        //Carga del fragment con el mapa.
        SupportMapFragment mapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.regLoc);
        mapFragment.getMapAsync(this);

        //Instancia del Geocoder.
        gc=new Geocoder(this);

        //Instancias de la autenticación y la base de datos de Firebase.
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        //Dependiendo de lo recibido por intent, hace una cosa u otra.
        if(getIntent().hasExtra("idPeluquero")){

            muestraLocPeluquero(getIntent().getStringExtra("idPeluquero"));

        }else{

            //Carga y guardado de los parámetros pasados desde la activity "RegistroActivity" mediante intent.
            email=getIntent().getStringExtra("email");
            pw=getIntent().getStringExtra("pw");
            nombre=getIntent().getStringExtra("nombre");
            telefono=Long.parseLong(getIntent().getStringExtra("telefono"));

            //Muestra un texto de registro en el respectivo botón y los activa.
            botonFinReg.setText(getString(R.string.botonRegistrar));
            activaBotones(true);

        }

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

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(final GoogleMap googleMap) {

        //Instancia del mapa.
        map=googleMap;

        //Comprobar si tenemos permiso de geolocalización para habilitar el botón de mi ubicación.
        if(comprobarPermisoLocalizacion() && comprobarPermisosLocalizacionAproximada()){

            //Si los permisos están activos, se muestra el botón de localización.
            botonLoc.setClickable(true);

            //Si lo que se va a realizar es un registro nuevo...
            if(botonFinReg.getText().toString().equals(getString(R.string.botonRegistrar))) {

                //Listener que se encarga de mostrar la localización GPS actual.
                LocationServices.getFusedLocationProviderClient(this).getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {

                                    //marca la localización actual en el mapa.
                                    marcarMapa(new LatLng(location.getLatitude(), location.getLongitude()));

                                } else {
                                    Toast.makeText(getApplicationContext(), getString(R.string.mensajeNoLoc),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), getString(R.string.mensajeNoLoc),
                                Toast.LENGTH_SHORT).show();
                    }
                });

            }

        }

        //Evento que recoge el toque del usuario en el mapa.
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                marcarMapa(latLng);

            }
        });

    }

    /**
     * Método que controla la activación de los botones de la activity.
     *
     * @param boleano Booleano true para activar los botones, y false para desactivarlos.
     */
    private void activaBotones(boolean boleano){

        botonFinReg.setEnabled(boleano);
        botonAtrasRegLoc.setEnabled(boleano);

    }

    /**
     * Método que se encarga de mostrar la localización guardada del peluquero
     *
     * @param idPeluquero Cadena con la ID del peluquero.
     */
    private void muestraLocPeluquero(String idPeluquero){

        //Obtiene los datos del peluquero desde su BD en Firestore.
        firestore.collection("peluqueros").document(idPeluquero).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {

                    //Si existen datos...
                    if (task.getResult().exists()) {

                        Peluquero peluquero = task.getResult().toObject(Peluquero.class);

                        //marca su localización guardada en el mapa...
                        marcarMapa(new LatLng(peluquero.getLoc().getLatitude(), peluquero.getLoc().getLongitude()));

                        //y muestra los datos extra de su localización.
                        etDatAdi.setText(peluquero.getLocExtra());

                    }
                }

                //Muestra un texto de guardar en el respectivo botón y los activa.
                botonFinReg.setText(getString(R.string.botonEditCuentaGuardar));
                activaBotones(true);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                botonFinReg.setText(getString(R.string.botonEditCuentaGuardar));
                activaBotones(true);
            }
        });

    }

    /**
     * Método que se encarga de generar la marca en el mapa.
     *
     * @param coordenadas LatLng con las coordenadas donde se va a generar la marca.
     */
    private void marcarMapa (LatLng coordenadas){

        loc=coordenadas;

        //Se crea una marca.
        MarkerOptions mo=new MarkerOptions();
        //Se le pasa la posición seleccionada.
        mo.position(loc);
        //Se le añade un título a la marca.
        mo.title(getString(R.string.mensajeMarca)+" "+loc.latitude+", "+loc.longitude);
        //Se borra la marca anterior.
        map.clear();
        //Se anima el movimiento hacia la nueva marca.
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
        //Se posiciona la marca en el lugar seleccionado.
        map.addMarker(mo);

        //Muestra la dirección postal en el campo correspondiente.
        obtieneDireccion();

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
     * Método que se encarga de traducir la dirección introducida en coordenadas.
     */
    protected void buscaCoordenadas(){

        try {
            //Obtiene la primera dirección obtenida.
            direcciones = gc.getFromLocationName(etDireccion.getText().toString(), 1);

            //Comprueba que se ha guardado una dirección.
            if(direcciones.size() > 0) {

                //Si es así, crea la marca en el mapa con las coordenadas obtenidas de la dirección.
                marcarMapa(new LatLng(direcciones.get(0).getLatitude(), direcciones.get(0).getLongitude()));

            }else{
                Toast.makeText(this, getString(R.string.mensajeNoDireccion), Toast.LENGTH_SHORT).show();
            }

        //Si el campo está vacío, salta la excepción que lo avisa.
        }catch(IOException ioe) {
            Toast.makeText(this, getString(R.string.mensajeDirVacia), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Método que se encarga de traducir las coordenadas en una dirección postal.
     */
    protected void obtieneDireccion() {

        try {

            //Obtiene la dirección mediante las coordenadas guardadas en la variable de la clase.
            direcciones = gc.getFromLocation(loc.latitude, loc.longitude, 1);

            //Comprueba que se ha guardado al menos una dirección.
            if(direcciones.size()>0){
                //Si es así, lo muestra en el EditText de dirección.
                etDireccion.setText(muestraDireccion(direcciones.get(0)));
            }else{
                //Si no, borra su contenido.
                etDireccion.setText("");
            }

        }catch (IOException ioe){
            Toast.makeText(this, getString(R.string.mensajeDirVacia), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Método que se encarga de generar una dirección postal más comprensible.
     *
     * @param direccion Objeto Address con la dirección postal.
     * @return Devuelve una cadena con la dirección postal simplificada.
     */
    protected String muestraDireccion(Address direccion) {

        String numero=direccion.getSubThoroughfare(),
                calle=direccion.getThoroughfare(),
                barrio=direccion.getSubLocality(),
                localidad=direccion.getLocality(),
                provincia=direccion.getSubAdminArea(),
                caOEstado=direccion.getAdminArea(),
                pais=direccion.getCountryName();

        StringBuffer sb=new StringBuffer();

        if(numero!=null){
            sb.append(numero+", ");
        }
        if(calle!=null){
            sb.append(calle+", ");
        }
        if(barrio!=null){
            sb.append(barrio+", ");
        }
        if(localidad!=null){
            sb.append(localidad+", ");
        }
        if(provincia!=null && !provincia.equalsIgnoreCase(localidad)){
            sb.append(provincia+", ");
        }
        if(caOEstado!=null){
            sb.append(caOEstado+", ");
        }
        if(pais!=null){
            sb.append(pais);
        }

        return sb.toString();

    }

    @SuppressLint("MissingPermission")
    /**
     * Método que se encarga de marcar la localización actual del dispositivo en el mapa.
     */
    protected void localizacion() {

        LocationManager locMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locMan.getLastKnownLocation(locMan.getBestProvider(new Criteria(), false));

        //Comprueba que la localización devuelta no es nula.
        if(location!=null) {
            marcarMapa(new LatLng(location.getLatitude(), location.getLongitude()));
        }else{
            Toast.makeText(this, getString(R.string.mensajeNoLoc), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Método que, al pulsar el botón de buscar, marca en el mapa la dirección introducida y
     * la muestra con un formato ofrecido por Google.
     *
     * @param view
     */
    public void buscaDireccion(View view){

        buscaCoordenadas();

    }

    /**
     * Método que, al pulsar el botón que finaliza el registro, crea el usuario e introduce su datos en la base de datos.
     *
     * @param view
     */
    public void registro(View view){

        //Comprueba que se ha guardado previamente unas coordenadas.
        if(loc!=null) {

            //Se desactiva el botón de registro y atrás para evitar varias pulsaciones simultáneas.
            activaBotones(false);

            //Si lo que se va a realizar es un registro nuevo...
            if (botonFinReg.getText().toString().equals(getString(R.string.botonRegistrar))) {

                //crea el usuario cliente con su email y contraseña.
                auth.createUserWithEmailAndPassword(email, pw)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                //Si se ha creado correctamente el usuario...
                                if (task.isSuccessful()) {

                                    //añade el nombre introducido a la cuenta creada (y loqueada)...
                                    auth.getCurrentUser().updateProfile(new UserProfileChangeRequest
                                            .Builder().setDisplayName(nombre).build());

                                    /*
                                    crea en la base de datos una colección de clientes (si aún no existe)
                                    y un registro (documento) con la id del cliente registrado. Se añaden
                                    también sus datos de registro mediante un POJO...
                                    */
                                    firestore.collection("peluqueros").document(auth.getCurrentUser()
                                            .getUid()).set(new Peluquero(nombre, telefono, new MiLatLng(loc.latitude,
                                            loc.longitude), etDatAdi.getText().toString()));

                                    //y le muestra un saludo con su nombre.
                                    Toast.makeText(getApplicationContext(), getString(R.string.regCompletado),
                                            Toast.LENGTH_SHORT).show();

                                    //Finalmente se cierra la activity.
                                    finish();

                                    //Si no...
                                } else {

                                    //captura el motivo, lo muestra...
                                    try {

                                        throw task.getException();

                                    } catch (FirebaseAuthUserCollisionException emailExistente) {
                                        Toast.makeText(getApplicationContext(),
                                                email + " " +
                                                        getText(R.string.mensajeEditCuentaExisteEmail),
                                                Toast.LENGTH_SHORT).show();
                                    } catch (Exception ex) {
                                        Toast.makeText(getApplicationContext(), getText(R.string.error_registro),
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    //y vuelve a activar el botón de registro y atrás.
                                    activaBotones(true);

                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), getText(R.string.error_registro),
                                Toast.LENGTH_SHORT).show();
                        activaBotones(true);
                    }
                });

                //Si lo que se va a realizar es una modificación de la localización...
            } else if (botonFinReg.getText().toString().equals(getString(R.string.botonEditCuentaGuardar))) {
                actualizaLocalizacion();
            }

        } else{
            Toast.makeText(getApplicationContext(), getText(R.string.mensajeDirVacia),
                    Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Método que cierra la activity.
     *
     * @param view
     */
    public void atras(View view){

        finish();

    }

    /**
     * Método que actualiza la localización del peluquero.
     */
    private void actualizaLocalizacion(){

        //Crea un Map con las modificaciones en los datos anidados de la localización.
        Map<String, Object> localizacion = new HashMap<>();
        localizacion.put("loc.latitude", loc.latitude);
        localizacion.put("loc.longitude", loc.longitude);

        //Actualiza las coordenadas en la BD del peluquero en Firestore.
        firestore.collection("peluqueros").document(auth.getCurrentUser().getUid())
                .update(localizacion)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                actualizaLocExtra();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getText(R.string.mensajeEditCuentaErrorLoc),
                        Toast.LENGTH_SHORT).show();
                activaBotones(true);
            }
        });

    }

    /**
     * Método que actualiza las indicaciones extra de la localización del peluquero.
     */
    private void actualizaLocExtra(){

        //Actualiza las indicaciones extra del peluquero en su BD de Firestore.
        firestore.collection("peluqueros").document(auth.getCurrentUser().getUid())
                .update("locExtra", etDatAdi.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toast.makeText(getApplicationContext(), getText(R.string.mensajeEditCuentaExitoLoc),
                        Toast.LENGTH_SHORT).show();

                //Devuelve los datos modificados a la activity anterior...
                Intent intentNuevaLoc =new Intent();
                intentNuevaLoc.putExtra("loc", etDireccion.getText().toString());
                intentNuevaLoc.putExtra("locExtra", etDatAdi.getText().toString());

                setResult(RESULT_OK, intentNuevaLoc);

                //y se cierra.
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getText(R.string.mensajeEditCuentaErrorLoc),
                        Toast.LENGTH_SHORT).show();
                activaBotones(true);
            }
        });

    }

}
