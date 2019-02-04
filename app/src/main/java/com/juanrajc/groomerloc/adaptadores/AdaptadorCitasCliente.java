package com.juanrajc.groomerloc.adaptadores;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.juanrajc.groomerloc.R;
import com.juanrajc.groomerloc.clasesBD.Cita;
import com.juanrajc.groomerloc.clasesBD.Peluquero;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AdaptadorCitasCliente extends RecyclerView.Adapter<AdaptadorCitasCliente.ViewHolder> implements View.OnClickListener {

    //Tipo de moneda usada.
    private final String MONEDA = " €";

    //Objeto del contexto de la aplicación.
    Context contexto;

    //Objeto del usuario actual y de la BD Firestore.
    private FirebaseUser usuario;
    private FirebaseFirestore firestore;

    //Objetos que contendrá las IDs y datos de las citas que se van a mostrar.
    private List<String> listaIdsCitas;
    private List<Cita> listaObjCitas;

    /**
     * Constructor del adaptador.
     *
     * @param listaIdsCitas Cadenas con las IDs de las citas.
     * @param listaObjCitas Objetos con los datos de las citas.
     */
    public AdaptadorCitasCliente(List<String> listaIdsCitas, List<Cita> listaObjCitas){

        //Instancia del usuario actual y de la base de datos Firestore.
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        this.listaIdsCitas=listaIdsCitas;
        this.listaObjCitas=listaObjCitas;

    }

    @NonNull
    @Override
    public AdaptadorCitasCliente.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        contexto = parent.getContext();

        return new AdaptadorCitasCliente.ViewHolder(LayoutInflater.from(contexto)
                .inflate(R.layout.cv_cita_cliente, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final AdaptadorCitasCliente.ViewHolder holder, final int position) {

        //Obtiene y muestra el nombre actual del peluquero.
        obtieneNombrePeluquero(listaObjCitas.get(position).getIdPeluquero(), holder);

        //Muestra el nombre del perro, el servicio solicitado y el precio establecido.
        holder.tvCitaClienteMasc.setText(listaObjCitas.get(position).getPerro().getNombre());
        holder.tvCitaClienteServ.setText(listaObjCitas.get(position).getServicio());
        holder.tvCitaClientePrec.setText(listaObjCitas.get(position).getPrecioFinal().toString()+MONEDA);

        //Comprueba si se ha establecido fecha y hora para la cita (si es así, la muestra).
        compruebaFechaConfirmacion(listaObjCitas.get(position).getFechaConfirmacion(), holder);

        //Listener de pulsación asignado a la tabla e imagen de la CardView.
        holder.tlCvCitaCliente.setOnClickListener(this);
        holder.ivCitaClienteConf.setOnClickListener(this);

    }

    /**
     * Método que se encarga de obtener y mostrar el nombre actual del peluquero al que
     * se le ha solicitado la cita. Si no puede obtenerlo, lo avisa en el mismo campo del nombre.
     *
     * @param idPeluquero Cadena con la ID del peluquero.
     * @param holder ViewHolder para poder manejar el campo de la vista donde se mostrará el nombre.
     */
    private void obtieneNombrePeluquero(String idPeluquero, final ViewHolder holder){

        //Obtiene los datos del peluquero desde Firestore mediante su ID.
        firestore.collection("peluqueros").document(idPeluquero).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){

                            //Si existe...
                            if(task.getResult().exists()) {

                                //muestra el nombre en el TextView correspondiente.
                                holder.tvCitaClientePelu.setText(task.getResult().toObject(Peluquero.class).getNombre());

                            }else{
                                holder.tvCitaClientePelu.setText(contexto.getString(R.string.citasNoExiste));
                            }

                        }else{
                            holder.tvCitaClientePelu.setText(contexto.getString(R.string.citasErrorCargaDato));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                holder.tvCitaClientePelu.setText(contexto.getString(R.string.citasErrorCargaDato));
            }
        });

    }

    /**
     * Método que se encarga de comprobar se se ha establecido una fecha para la cita. Si es así,
     * la muestra en el campo correspondiente y colorea el ImageView del color correspondiente.
     *
     * @param fechaConfirmacion Date con la fecha y hora de la cita (si está establecida).
     * @param holder ViewHolder para poder manejar el campo de la vista donde se mostrará la fecha.
     */
    private void compruebaFechaConfirmacion(Date fechaConfirmacion, ViewHolder holder){

        //Si el campo no es nulo...
        if(fechaConfirmacion!=null){
            //muestra la fecha formateada y colorea el ImageView de color verde...
            holder.tvCitaClienteFech.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(fechaConfirmacion));
            holder.ivCitaClienteConf.setBackgroundColor(contexto.getResources().getColor(R.color.colorFechaCitaConf));
        //si es nulo...
        }else{
            //avisa que no está establecida en el mismo campo y colorea el ImageView de color rojo.
            holder.tvCitaClienteFech.setText(contexto.getString(R.string.citasSinConfirmar));
            holder.ivCitaClienteConf.setBackgroundColor(contexto.getResources().getColor(R.color.colorFechaCitaNoConf));
        }

    }

    @Override
    public int getItemCount() {

        if(listaIdsCitas!=null) {
            return listaIdsCitas.size();
        }else{
            return 0;
        }

    }

    @Override
    public void onClick(View view) {



    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TableLayout tlCvCitaCliente;

        public TextView tvCitaClientePelu,tvCitaClienteMasc, tvCitaClienteServ,
                tvCitaClientePrec, tvCitaClienteFech;

        public ImageView ivCitaClienteConf;

        public ViewHolder(View itemView) {
            super(itemView);

            tlCvCitaCliente = (TableLayout) itemView.findViewById(R.id.tlCvCitaCliente);

            tvCitaClientePelu = (TextView) itemView.findViewById(R.id.tvCitaClientePelu);
            tvCitaClienteMasc = (TextView) itemView.findViewById(R.id.tvCitaClienteMasc);
            tvCitaClienteServ = (TextView) itemView.findViewById(R.id.tvCitaClienteServ);
            tvCitaClientePrec = (TextView) itemView.findViewById(R.id.tvCitaClientePrec);
            tvCitaClienteFech = (TextView) itemView.findViewById(R.id.tvCitaClienteFech);

            ivCitaClienteConf = (ImageView) itemView.findViewById(R.id.ivCitaClienteConf);

        }

    }

}
