package com.juanrajc.groomerloc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class RegistroLocActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Coordenadas de España.
    private final LatLng espana = new LatLng(40.46366700000001, -3.7492200000000366);

    GoogleMap map;

    //Coordenadas de la marca creada por el usuario.
    LatLng loc=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_loc);

        SupportMapFragment mapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.regLoc);
        mapFragment.getMapAsync(this);

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
                    moverCamara(new LatLng(location.getLatitude(), location.getLongitude()), 18f);
                }
            });

            //Si no, mostramos la Península Ibérica.
        }else{
            //posicionamiento de la camara en españa
            moverCamara(espana, 5.5f);
        }

        //Evento que recoge el toque del usuario en el mapa.
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                //Se guarda la posición seleccionada en una variable de clase.
                loc=latLng;

                //Se crea una marca.
                MarkerOptions mo=new MarkerOptions();
                //Se le pasa la posición seleccionada.
                mo.position(latLng);
                //Se le añade un título a la marca.
                mo.title(getString(R.string.mensajeMarca)+latLng.latitude+", "+latLng.longitude);
                //Se borra la marca anterior.
                map.clear();
                //Se anima el movimiento hacia la nueva marca.
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                //Se posiciona la marca en el lugar seleccionado.
                map.addMarker(mo);

            }
        });

    }

    /**
     * Método para mover la camara del mapa a las coordenadas pasadas por parámetros.
     *
     * @param latLng Coordenadas hacia donde se va a mover la cámara.
     * @param zoom Cantidad de zoom que se le va a proporcionar.
     */
    private void moverCamara(LatLng latLng, float zoom){
        CameraPosition cameraPosition =
                CameraPosition.builder()
                        .target(latLng)
                        .zoom(zoom).build();

        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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

    protected void atras(View view){

        finish();

    }

    protected void registro(View view){



    }

}
