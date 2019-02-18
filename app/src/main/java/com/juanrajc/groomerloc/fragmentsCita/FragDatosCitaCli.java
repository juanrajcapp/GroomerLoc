package com.juanrajc.groomerloc.fragmentsCita;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.juanrajc.groomerloc.CitaClienteActivity;
import com.juanrajc.groomerloc.R;
import com.juanrajc.groomerloc.clasesBD.Cita;
import com.juanrajc.groomerloc.clasesBD.Peluquero;
import com.juanrajc.groomerloc.recursos.GlideApp;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class FragDatosCitaCli extends Fragment implements OnMapReadyCallback {

    //Objeto del mapa que se muestra en la activity.
    private GoogleMap map;

    //Tipo de moneda usada y unidad de peso.
    private final String MONEDA = " €", PESO = " Kg";

    //Objetos de los elementos de la vista.
    private TextView tvCitaCliFechCrea, tvCitaCliFech, tvCitaCliServ, tvCitaCliPrecio,
            tvCitaCliPelu, tvCitaCliPeluTlfn, tvCitaCliPeluLoc, tvCitaCliPeluLocExt,
            tvCitaCliPerro, tvCitaCliRaza, tvCitaCliSexo, tvCitaCliPeso, tvCitaCliComent;

    //Objeto de la imagen del perro.
    private ImageView ivCitaCliPerro;

    //Objeto del círculo de carga.
    private ProgressBar circuloCargaCitaCli;

    //Geocoder para la traducción de coordenadas en direciones y viceversa.
    private Geocoder gc;

    //ID de la cita a mostrar.
    private String idCita;

    //Objeto del usuario actual y de la BD Firestore.
    private FirebaseUser usuario;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_datos_cita_cli, container, false);

        //Carga del fragment con el mapa.
        SupportMapFragment mapFragment=(SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapCitaCli);
        mapFragment.getMapAsync(this);

        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Instancia del usuario actual y de la base de datos Firestore.
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        //Instancia del círculo de carga.
        circuloCargaCitaCli = getActivity().findViewById(R.id.circuloCargaCitaCli);

        //Instancias de los elementos de la vista.
        tvCitaCliFechCrea = getActivity().findViewById(R.id.tvCitaCliFechCrea);
        tvCitaCliFech = getActivity().findViewById(R.id.tvCitaCliFech);
        tvCitaCliServ = getActivity().findViewById(R.id.tvCitaCliServ);
        tvCitaCliPrecio = getActivity().findViewById(R.id.tvCitaCliPrecio);
        tvCitaCliPelu = getActivity().findViewById(R.id.tvCitaCliPelu);
        tvCitaCliPeluTlfn = getActivity().findViewById(R.id.tvCitaCliPeluTlfn);
        tvCitaCliPeluLoc = getActivity().findViewById(R.id.tvCitaCliPeluLoc);
        tvCitaCliPeluLocExt = getActivity().findViewById(R.id.tvCitaCliPeluLocExt);
        tvCitaCliPerro = getActivity().findViewById(R.id.tvCitaCliPerro);
        tvCitaCliRaza = getActivity().findViewById(R.id.tvCitaCliRaza);
        tvCitaCliSexo = getActivity().findViewById(R.id.tvCitaCliSexo);
        tvCitaCliPeso = getActivity().findViewById(R.id.tvCitaCliPeso);
        tvCitaCliComent = getActivity().findViewById(R.id.tvCitaCliComent);

        //Instancia del ImageView que va a mostrar la foto del perro.
        ivCitaCliPerro = getActivity().findViewById(R.id.ivCitaCliPerro);

        //Instancia del Geocoder.
        gc=new Geocoder(getActivity());

        //Recoge la ID de la cita desde la activity que carga este fragment.
        idCita = ((CitaClienteActivity)getContext()).getIdCita();

        obtieneCita();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //Instancia del mapa.
        map=googleMap;

        //Activa los controles de zoom del mapa.
        map.getUiSettings().setZoomControlsEnabled(true);
        //Desactiva la respuesta a los gestos con los dedos sobre el mapa.
        map.getUiSettings().setAllGesturesEnabled(false);

        //Comprueba si la aplicación tiene permisos de geolocalización.
        if((ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
                &&
                (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED)){

            //Si es así, activa y muestra la localización del usuario.
            googleMap.setMyLocationEnabled(true);

        }

    }

    /**
     * Método que obtiene los datos de la cita.
     */
    private void obtieneCita(){

        //Se visibiliza el círculo de carga.
        circuloCargaCitaCli.setVisibility(View.VISIBLE);

        //Obtiene los datos de la cita mediante su ID desde Firestore.
        firestore.collection("citas").document(idCita).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){

                            //Si la cita existe...
                            if(task.getResult().exists()){

                                //Crea un objeto con los datos obtenidos...
                                Cita cita = task.getResult().toObject(Cita.class);

                                muestraCita(cita);

                                //y obtiene la fotografía desde Firebase Storage (si existe).
                                GlideApp.with(getActivity().getApplicationContext())
                                        .load(FirebaseStorage.getInstance().getReference()
                                                .child("citas/" + idCita + "/perros/" + cita.getPerro().getNombre()
                                                        +" "+cita.getPerro().getFechaFoto() + ".jpg"))
                                        .apply(new RequestOptions().placeholder(R.drawable.icono_mascota)
                                                .error(R.drawable.icono_mascota))
                                        .into(ivCitaCliPerro);

                                //Finalizada la carga, se vuelve a invisibilizar el círculo de carga.
                                circuloCargaCitaCli.setVisibility(View.INVISIBLE);

                            }else{
                                Toast.makeText(getActivity().getApplicationContext(),
                                        getText(R.string.mensajeCitaNoExiste),
                                        Toast.LENGTH_LONG).show();
                                getActivity().finish();
                            }

                        }else{
                            Toast.makeText(getActivity().getApplicationContext(),
                                    getText(R.string.mensajeCitaNoCarga),
                                    Toast.LENGTH_LONG).show();
                            getActivity().finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity().getApplicationContext(),
                        getText(R.string.mensajeCitaNoCarga),
                        Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        });

    }

    /**
     * Método que muestra los datos de la cita en la vista del fragment.
     *
     * @param cita Objeto tipo Cita con los datos obtenidos desde Firestore.
     */
    private void muestraCita(Cita cita){

        //Muestra la fecha de creación formateada.
        tvCitaCliFechCrea.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm")
                .format(cita.getFechaCreacion()));

        //Muestra la fecha de confirmación formateada (si existe).
        if(cita.getFechaConfirmacion()!=null){
            tvCitaCliFech.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm")
                    .format(cita.getFechaConfirmacion()));
        }else{
            tvCitaCliFech.setText(getActivity().getString(R.string.citasSinConfirmar));
        }

        //Muestra el servicio solicitado después de ser formateado.
        tvCitaCliServ.setText(obtieneServicio(cita.getServicios()));

        //Muestra el precio final del servicio.
        tvCitaCliPrecio.setText(cita.getPrecioFinal().toString()+MONEDA);

        obtienePeluquero(cita.getIdPeluquero());

        //Muestra los datos del perro.
        tvCitaCliPerro.setText(cita.getPerro().getNombre());
        tvCitaCliRaza.setText(cita.getPerro().getRaza());
        tvCitaCliSexo.setText(traduceSexo(cita.getPerro().getSexo()));
        tvCitaCliPeso.setText(((Float) cita.getPerro().getPeso()).toString()+PESO);
        tvCitaCliComent.setText(cita.getPerro().getComentario());

    }

    /**
     * Método que se encarga de cargar y mostrar los datos del peluquero.
     *
     * @param idPeluquero Cadena con la ID del peluquero.
     */
    private void obtienePeluquero(String idPeluquero){

        //Obtiene los datos del peluquero mediante su ID desde Firestore.
        firestore.collection("peluqueros").document(idPeluquero).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){

                            //Si el peluquero existe...
                            if(task.getResult().exists()){

                                //Crea un objeto con los datos obtenidos...
                                Peluquero peluquero= task.getResult().toObject(Peluquero.class);

                                //y los muestra en la vista del fragment.
                                tvCitaCliPelu.setText(peluquero.getNombre());
                                tvCitaCliPeluTlfn.setText(((Long) peluquero.getTelefono()).toString());
                                tvCitaCliPeluLoc.setText(obtieneDireccion(new LatLng(peluquero.getLoc()
                                        .getLatitude(), peluquero.getLoc().getLongitude())));
                                tvCitaCliPeluLocExt.setText(peluquero.getLocExtra());

                                marcaPeluquero(new LatLng(peluquero.getLoc().getLatitude(),
                                        peluquero.getLoc().getLongitude()), peluquero.getNombre());

                            }else{
                                tvCitaCliPelu.setText(getActivity().getString(R.string.citasNoExiste));
                                tvCitaCliPeluTlfn.setText(getActivity().getString(R.string.citasNoExiste));
                                tvCitaCliPeluLoc.setText(getActivity().getString(R.string.citasNoExiste));
                                tvCitaCliPeluLocExt.setText(getActivity().getString(R.string.citasNoExiste));
                            }

                        }else{
                            tvCitaCliPelu.setText(getActivity().getString(R.string.citasErrorCargaDato));
                            tvCitaCliPeluTlfn.setText(getActivity().getString(R.string.citasErrorCargaDato));
                            tvCitaCliPeluLoc.setText(getActivity().getString(R.string.citasErrorCargaDato));
                            tvCitaCliPeluLocExt.setText(getActivity().getString(R.string.citasErrorCargaDato));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                tvCitaCliPelu.setText(getActivity().getString(R.string.citasErrorCargaDato));
                tvCitaCliPeluTlfn.setText(getActivity().getString(R.string.citasErrorCargaDato));
                tvCitaCliPeluLoc.setText(getActivity().getString(R.string.citasErrorCargaDato));
                tvCitaCliPeluLocExt.setText(getActivity().getString(R.string.citasErrorCargaDato));
            }
        });

    }

    /**
     * Método que se encarga de traducir las coordenadas en una dirección postal.
     *
     * @param coordenadas LatLng con las coordenadas que se quieren traducir.
     *
     * @return Cadena con la dirección postal obtenida mediante las coordenadas.
     */
    private String obtieneDireccion(LatLng coordenadas) {

        try {

            //Obtiene la dirección mediante las coordenadas obtenidas por parámetro.
            List<Address> direcciones = gc.getFromLocation(coordenadas.latitude, coordenadas.longitude, 1);

            //Comprueba que se ha guardado al menos una dirección.
            if(direcciones.size()>0){
                //Si es así, devuelve el primero obtenido.
                return muestraDireccion(direcciones.get(0));
            }else{
                //Si no, devuelve una cadena vacía.
                return "";
            }

        }catch (IOException ioe){
            return "";
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

    /**
     * Método que marca y mueve la cámara del mapa a las coordenadas del peluquero.
     *
     * @param locPeluquero LatLng con las coordenadas del peluquero.
     * @param nombrePeluquero Cadena con el nombre del peluquero.
     */
    private void marcaPeluquero(LatLng locPeluquero, String nombrePeluquero){

        //Se crea una marca...
        MarkerOptions mo=new MarkerOptions();
        //se le pasa la posición del peluquero...
        mo.position(locPeluquero);
        //se crea la marca...
        map.addMarker(mo).setTag(nombrePeluquero);
        //y se mueve la cámara a dicha marca.
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(locPeluquero, 15f));

    }

    /**
     * Método que traduce la lista de servicios guardada en valores enteros en una cadena con
     * la descripción de todos los servicios solicitados en la cita.
     *
     * @param listaServicios Lista de enteros, los cuales significan un servicio solicitado en la cita.
     *
     * @return Cadena con la descripción de todos los servicios solicitados en la cita.
     */
    private String obtieneServicio(List<Integer> listaServicios){

        StringBuffer servicio=new StringBuffer();

        for(Integer numServicio:listaServicios){

            switch (numServicio){

                case 1:
                    servicio.append(getActivity().getString(R.string.servicioBanio));
                    break;

                case 2:
                    servicio.append(getActivity().getString(R.string.servicioArreglo));
                    break;

                case 3:
                    servicio.append(getActivity().getString(R.string.servicioCorte));
                    break;

                case 4:
                    servicio.append(getActivity().getString(R.string.servicioDeslanado));
                    break;

                case 5:
                    servicio.append(getActivity().getString(R.string.servicioTinte));
                    break;

                case 6:
                    servicio.append(getActivity().getString(R.string.servicioOidos));
                    break;

                case 7:
                    servicio.append(getActivity().getString(R.string.servicioUnias));
                    break;

                case 8:
                    servicio.append(getActivity().getString(R.string.servicioAnales));
                    break;

            }

        }

        return servicio.toString();

    }

    /**
     * Método que traduce el valor del sexo obtenido desde Firestore en una cadena más comprensible.
     *
     * @param sexo Cadena con el valor del sexo obtenido desde Firestore.
     *
     * @return Cadena con el sexo en un formato más comprensible.
     */
    private String traduceSexo(String sexo){

        if(sexo.equalsIgnoreCase("XX")){
            return getActivity().getString(R.string.hembra);
        }else if(sexo.equalsIgnoreCase("XY")){
            return getActivity().getString(R.string.macho);
        }else{
            return "?";
        }

    }

}
