package com.juanrajc.groomerloc.fragmentsCita;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.juanrajc.groomerloc.CitaPeluActivity;
import com.juanrajc.groomerloc.R;
import com.juanrajc.groomerloc.clasesBD.Cita;
import com.juanrajc.groomerloc.clasesBD.Cliente;
import com.juanrajc.groomerloc.recursos.GlideApp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class FragDatosCitaPelu extends Fragment {

    //Tipo de moneda usada y unidad de peso.
    private final String MONEDA = " €", PESO = " Kg";

    //Objetos de los elementos de la vista.
    private TextView tvCitaPeluFechCrea, tvCitaPeluFech, tvCitaPeluServ, tvCitaPeluPrecio,
            tvCitaPeluCli, tvCitaPeluCliTlfn, tvCitaPeluPerro, tvCitaPeluRaza, tvCitaPeluSexo,
            tvCitaPeluPeso, tvCitaPeluComent;

    //Objeto del botón de la vista.
    private Button botonCitaPeluFecha;

    //Objeto de la imagen del perro.
    private ImageView ivCitaPeluPerro;

    //Objeto del círculo de carga.
    private ProgressBar circuloCargaCitaPelu;

    //ID de la cita a mostrar.
    private String idCita;

    //Objeto de la BD Firestore.
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_datos_cita_pelu, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Instancia de la base de datos Firestore.
        firestore = FirebaseFirestore.getInstance();

        //Instancia del círculo de carga.
        circuloCargaCitaPelu = getActivity().findViewById(R.id.circuloCargaCitaPelu);

        //Instancias de los elementos de la vista.
        tvCitaPeluFechCrea = getActivity().findViewById(R.id.tvCitaPeluFechCrea);
        tvCitaPeluFech = getActivity().findViewById(R.id.tvCitaPeluFech);
        tvCitaPeluServ = getActivity().findViewById(R.id.tvCitaPeluServ);
        tvCitaPeluPrecio = getActivity().findViewById(R.id.tvCitaPeluPrecio);
        tvCitaPeluCli = getActivity().findViewById(R.id.tvCitaPeluCli);
        tvCitaPeluCliTlfn = getActivity().findViewById(R.id.tvCitaPeluCliTlfn);
        tvCitaPeluPerro = getActivity().findViewById(R.id.tvCitaPeluPerro);
        tvCitaPeluRaza = getActivity().findViewById(R.id.tvCitaPeluRaza);
        tvCitaPeluSexo = getActivity().findViewById(R.id.tvCitaPeluSexo);
        tvCitaPeluPeso = getActivity().findViewById(R.id.tvCitaPeluPeso);
        tvCitaPeluComent = getActivity().findViewById(R.id.tvCitaPeluComent);

        //Instancia del ImageView que va a mostrar la foto del perro.
        ivCitaPeluPerro = getActivity().findViewById(R.id.ivCitaPeluPerro);

        //Instancia y desactivación del botón con el que se establece la fecha de la cita.
        botonCitaPeluFecha = getActivity().findViewById(R.id.botonCitaPeluFecha);
        botonCitaPeluFecha.setEnabled(false);

        //Recoge la ID de la cita desde la activity que carga este fragment.
        idCita = ((CitaPeluActivity)getContext()).getIdCita();

        botonCitaPeluFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pideFechaCita();
            }
        });

        obtieneCita();

    }

    /**
     * Método que obtiene los datos de la cita.
     */
    private void obtieneCita(){

        //Se visibiliza el círculo de carga.
        circuloCargaCitaPelu.setVisibility(View.VISIBLE);

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
                                        .into(ivCitaPeluPerro);

                                //Finalizada la carga, se vuelve a invisibilizar el círculo de carga...
                                circuloCargaCitaPelu.setVisibility(View.INVISIBLE);
                                //y se activa el botón para establecer una fecha.
                                botonCitaPeluFecha.setEnabled(true);

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
        tvCitaPeluFechCrea.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm")
                .format(cita.getFechaCreacion()));

        //Muestra la fecha de confirmación formateada (si existe).
        if(cita.getFechaConfirmacion()!=null){
            tvCitaPeluFech.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm")
                    .format(cita.getFechaConfirmacion()));
        }else{
            tvCitaPeluFech.setText(getActivity().getString(R.string.citasSinConfirmar));
        }

        //Muestra el servicio solicitado después de ser formateado.
        tvCitaPeluServ.setText(obtieneServicio(cita.getServicios()));

        //Muestra el precio final del servicio.
        tvCitaPeluPrecio.setText(cita.getPrecioFinal().toString()+MONEDA);

        obtieneCliente(cita.getIdCliente());

        //Muestra los datos del perro.
        tvCitaPeluPerro.setText(cita.getPerro().getNombre());
        tvCitaPeluRaza.setText(cita.getPerro().getRaza());
        tvCitaPeluSexo.setText(traduceSexo(cita.getPerro().getSexo()));
        tvCitaPeluPeso.setText(((Float) cita.getPerro().getPeso()).toString()+PESO);
        tvCitaPeluComent.setText(cita.getPerro().getComentario());

    }

    /**
     * Método que se encarga de cargar y mostrar los datos del cliente.
     *
     * @param idCliente Cadena con la ID del cliente.
     */
    private void obtieneCliente(String idCliente){

        //Obtiene los datos del cliente mediante su ID desde Firestore.
        firestore.collection("clientes").document(idCliente).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){

                            //Si el cliente existe...
                            if(task.getResult().exists()){

                                //Crea un objeto con los datos obtenidos...
                                Cliente cliente = task.getResult().toObject(Cliente.class);

                                //y los muestra en la vista del fragment.
                                tvCitaPeluCli.setText(cliente.getNombre());
                                tvCitaPeluCliTlfn.setText(((Long) cliente.getTelefono()).toString());

                            }else{
                                tvCitaPeluCli.setText(getActivity().getString(R.string.citasNoExiste));
                                tvCitaPeluCliTlfn.setText(getActivity().getString(R.string.citasNoExiste));
                            }

                        }else{
                            tvCitaPeluCli.setText(getActivity().getString(R.string.citasErrorCargaDato));
                            tvCitaPeluCliTlfn.setText(getActivity().getString(R.string.citasErrorCargaDato));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                tvCitaPeluCli.setText(getActivity().getString(R.string.citasErrorCargaDato));
                tvCitaPeluCliTlfn.setText(getActivity().getString(R.string.citasErrorCargaDato));
            }
        });

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

    /**
     * Método que guarda y muestra la nueva fecha establecida para la cita.
     *
     * @param nuevaFecha Date con la nueva fecha seleccionada por el usuario.
     */
    private void estableceFechaCita(final Date nuevaFecha){

        //Modifica la fecha de la cita en Firestore.
        firestore.collection("citas").document(idCita)
                .update("fechaConfirmacion", nuevaFecha)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            //Muestra la nueva fecha establecida para la cita.
                            tvCitaPeluFech.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm")
                                    .format(nuevaFecha));

                            Toast.makeText(getActivity().getApplicationContext(),
                                    getText(R.string.mensajeCitaFechaExito),
                                    Toast.LENGTH_LONG).show();

                        }else{
                            Toast.makeText(getActivity().getApplicationContext(),
                                    getText(R.string.mensajeCitaFechaFallo),
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity().getApplicationContext(),
                        getText(R.string.mensajeCitaFechaFallo),
                        Toast.LENGTH_LONG).show();
            }
        });

    }

    /**
     * Método que muestra un diálogo con el que se podrá establecer una nueva fecha para la cita.
     */
    private void pideFechaCita(){

        final View dvFechaHora = View.inflate(getActivity(), R.layout.obtiene_fecha, null);
        final AlertDialog adFechaHora = new AlertDialog.Builder(getActivity()).create();

        //Listener para el botón de establecer fecha.
        dvFechaHora.findViewById(R.id.establecer_boton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Obtiene la fecha y hora seleccionada por el usuario...
                DatePicker datePicker = (DatePicker) dvFechaHora.findViewById(R.id.fecha_picker);
                TimePicker timePicker = (TimePicker) dvFechaHora.findViewById(R.id.hora_picker);

                //crea un objeto Calendar con la fecha y hora obtenidas...
                Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                        datePicker.getMonth(),
                        datePicker.getDayOfMonth(),
                        timePicker.getCurrentHour(),
                        timePicker.getCurrentMinute());

                //crea un objeto de tipo Date...
                Date fechaCita = new Date();

                //y establece la fecha y hora del Date pasándole los milisegundos del anterior Calendar.
                fechaCita.setTime(calendar.getTimeInMillis());

                //Comprueba que sea una fecha válida.
                if(compruebaValidezFecha(fechaCita)){

                    estableceFechaCita(fechaCita);

                    adFechaHora.dismiss();

                }else{

                    Toast.makeText(getActivity().getApplicationContext(),
                            getText(R.string.mensajeCitaFechaNoValida),
                            Toast.LENGTH_LONG).show();

                }

            }});

        //Listener para el botón de salir del diálogo.
        dvFechaHora.findViewById(R.id.salir_boton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adFechaHora.dismiss();
            }
        });

        adFechaHora.setView(dvFechaHora);
        adFechaHora.show();

    }

    /**
     * Método que compueba que la fecha y hora recibida es posterior a la actual.
     *
     * @param fecha Date con la fecha y hora a comprobar.
     *
     * @return Boolean true si la fecha es válida (es posterior a la actual).
     */
    private boolean compruebaValidezFecha(Date fecha){

        //Obtiene la diferencia de tiempo en milisegundos entre la fecha actual y la recibida.
        long tiempo = Calendar.getInstance().getTime().getTime() - fecha.getTime();

        //Si es un valor negativo significa que la fecha es posterior a la actual.
        if(tiempo<0){
            return true;
        }else{
            return false;
        }

    }

}
