package com.juanrajc.groomerloc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;

public class RegistroLocActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map;

    Geocoder gc;
    List<Address> direcciones;

    //Coordenadas de la marca creada por el usuario.
    LatLng loc=null;

    private EditText etDireccion, etDatAdi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_loc);

        etDireccion = (EditText) findViewById(R.id.etDireccion);
        etDatAdi = (EditText) findViewById(R.id.etDatAdi);

        SupportMapFragment mapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.regLoc);
        mapFragment.getMapAsync(this);

        gc=new Geocoder(this);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("recupLoc", loc);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        loc=(LatLng) savedInstanceState.getParcelable("recupLoc");

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(final GoogleMap googleMap) {

        map=googleMap;

        //Comprobar si tenemos permiso de geolocalización para habilitar el botón de mi ubicación
        if(comprobarPermisoLocalizacion() && comprobarPermisosLocalizacionAproximada()){

            //Si es así, activamos y mostramos la localización del usuario.
            googleMap.setMyLocationEnabled(true);

            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
            client.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
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
        map.animateCamera(CameraUpdateFactory.newLatLng(loc));
        //Se posiciona la marca en el lugar seleccionado.
        map.addMarker(mo);

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

    protected void buscaCoordenadas(View view){

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

    protected void atras(View view){

        finish();

    }

    protected void registro(View view){



    }

}
