package com.juanrajc.groomerloc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.juanrajc.groomerloc.clasesBD.Cliente;
import com.juanrajc.groomerloc.clasesBD.Peluquero;

import java.io.IOException;
import java.util.List;

public class RegistroLocActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map;

    Geocoder gc;
    List<Address> direcciones;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    //Coordenadas de la marca creada por el usuario.
    LatLng loc=null;

    private String email, pw, nombre;
    private int telefono;

    private EditText etDireccion, etDatAdi;
    private ImageButton botonLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_loc);

        etDireccion = (EditText) findViewById(R.id.etDireccion);
        etDatAdi = (EditText) findViewById(R.id.etDatAdi);

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

        SupportMapFragment mapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.regLoc);
        mapFragment.getMapAsync(this);

        gc=new Geocoder(this);

        //Instancias de la autenticación y la base de datos de Firebase.
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        email=getIntent().getStringExtra("email");
        pw=getIntent().getStringExtra("pw");
        nombre=getIntent().getStringExtra("nombre");
        telefono=getIntent().getIntExtra("telefono", 0);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            finish();
            startActivity(getIntent());
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(final GoogleMap googleMap) {

        map=googleMap;

        //Comprobar si tenemos permiso de geolocalización para habilitar el botón de mi ubicación
        if(comprobarPermisoLocalizacion() && comprobarPermisosLocalizacionAproximada()){

            //Si los permisos están activos, se muestra el botón de localización.
            botonLoc.setClickable(true);

            //Listener que se encarga de marcar la localización GPS actual.
            LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location!=null) {

                        marcarMapa(new LatLng(location.getLatitude(), location.getLongitude()));

                    }
                }
            });
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
        mo.title(getString(R.string.mensajeMarca)+loc.latitude+", "+loc.longitude);
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
            direcciones = gc.getFromLocationName(etDireccion.getText().toString(), 1);

            if(direcciones.size() > 0) {
                marcarMapa(new LatLng(direcciones.get(0).getLatitude(), direcciones.get(0).getLongitude()));
            }else{
                Toast.makeText(this, getString(R.string.mensajeNoDireccion), Toast.LENGTH_SHORT).show();
            }

        }catch(IOException ioe) {
            Toast.makeText(this, getString(R.string.mensajeDirVacia), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Método que se encarga de traducir las coordenadas en una dirección postal.
     */
    protected void obtieneDireccion() {

        try {

            direcciones = gc.getFromLocation(loc.latitude, loc.longitude, 1);

            if(direcciones.size()>0){
                etDireccion.setText(muestraDireccion(direcciones.get(0)));
            }else{
                etDireccion.setText("");
            }

        }catch (IOException ioe){

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
        Criteria criteria = new Criteria();
        Location location = locMan.getLastKnownLocation(locMan.getBestProvider(criteria, false));
        marcarMapa(new LatLng(location.getLatitude(), location.getLongitude()));

    }

    protected void atras(View view){

        finish();

    }

    protected void registro(View view){

        //crea el usuario cliente con su email y contraseña...
        auth.createUserWithEmailAndPassword(email, pw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                //cuando se ha creado, añade el nombre introducido a la cuenta creada (y loqueada)...
                auth.getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(nombre).build());

                /*
                crea en la base de datos una colección de clientes (si aún no existe) y un registro (documento)
                con la id del cliente registrado. Se añaden también sus datos de registro mediante un POJO...
                */
                firestore.collection("peluqueros")
                        .document(auth.getCurrentUser().getUid()).set(new Peluquero(telefono, loc));

                //finalmente se cierra la activity.
                finish();

                }
        });

    }

}
