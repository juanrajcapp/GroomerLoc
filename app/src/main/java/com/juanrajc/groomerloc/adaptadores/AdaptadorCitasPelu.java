package com.juanrajc.groomerloc.adaptadores;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.juanrajc.groomerloc.clasesBD.Cliente;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AdaptadorCitasPelu extends RecyclerView.Adapter<AdaptadorCitasPelu.ViewHolder> implements View.OnClickListener {

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
    public AdaptadorCitasPelu(List<String> listaIdsCitas, List<Cita> listaObjCitas){

        //Instancia del usuario actual y de la base de datos Firestore.
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        this.listaIdsCitas=listaIdsCitas;
        this.listaObjCitas=listaObjCitas;

    }

    @NonNull
    @Override
    public AdaptadorCitasPelu.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        contexto = parent.getContext();

        return new AdaptadorCitasPelu.ViewHolder(LayoutInflater.from(contexto)
                .inflate(R.layout.cv_cita_pelu, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final AdaptadorCitasPelu.ViewHolder holder, final int position) {

        //Comprueba si se ha establecido fecha y hora para la cita.
        compruebaFechaConfirmacion(listaObjCitas.get(position).getFechaConfirmacion(),
                listaObjCitas.get(position).getFechaCreacion(), holder);

        //Obtiene y muestra el nombre actual del cliente.
        obtieneNombreCliente(listaObjCitas.get(position).getIdCliente(), holder);

        //Muestra el nombre del perro, el servicio solicitado y el precio establecido.
        holder.tvCitaPeluMasc.setText(listaObjCitas.get(position).getPerro().getNombre());
        holder.tvCitaPeluServ.setText(listaObjCitas.get(position).getServicio());
        holder.tvCitaPeluPrec.setText(listaObjCitas.get(position).getPrecioFinal().toString()+MONEDA);

        //Listener de pulsación asignado a la tabla e imagen de la CardView.
        holder.tlCvCitaPelu.setOnClickListener(this);

    }

    /**
     * Método que se encarga de comprobar se se ha confirmado una fecha para la cita. Si es así,
     * la muestra en el campo correspondiente, si no, muestra la fecha de creación de la cita.
     *
     * @param fechaConfirmacion Date con la fecha y hora de la cita (si está establecida).
     * @param fechaCreacion Date con la fecha y hora de creación de la cita.
     * @param holder ViewHolder para poder manejar el campo de la vista donde se mostrará la fecha.
     */
    private void compruebaFechaConfirmacion(Date fechaConfirmacion, Date fechaCreacion, AdaptadorCitasPelu.ViewHolder holder){

        //Si el campo no es nulo...
        if(fechaConfirmacion!=null){
            //muestra la fecha de la cita formateada...
            holder.tvCitaPeluTitFech.setText(contexto.getString(R.string.cvCitasPeluFechaConf));
            holder.tvCitaPeluFech.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(fechaConfirmacion));
        //si es nulo...
        }else{
            //muestra la fecha de solicitud formateada.
            holder.tvCitaPeluTitFech.setText(contexto.getString(R.string.cvCitasPeluFechaCrea));
            holder.tvCitaPeluFech.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(fechaCreacion));
        }

    }

    /**
     * Método que se encarga de obtener y mostrar el nombre actual del cliente que
     * ha solicitado la cita. Si no puede obtenerlo, lo avisa en el mismo campo del nombre.
     *
     * @param idCliente Cadena con la ID del cliente.
     * @param holder ViewHolder para poder manejar el campo de la vista donde se mostrará el nombre.
     */
    private void obtieneNombreCliente(String idCliente, final AdaptadorCitasPelu.ViewHolder holder){

        //Obtiene los datos del cliente desde Firestore mediante su ID.
        firestore.collection("clientes").document(idCliente).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){

                            //Si existe...
                            if(task.getResult().exists()) {

                                //muestra el nombre en el TextView correspondiente.
                                holder.tvCitaPeluCli.setText(task.getResult().toObject(Cliente.class).getNombre());

                            }else{
                                holder.tvCitaPeluCli.setText(contexto.getString(R.string.citasNoExiste));
                            }

                        }else{
                            holder.tvCitaPeluCli.setText(contexto.getString(R.string.citasErrorCargaDato));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                holder.tvCitaPeluCli.setText(contexto.getString(R.string.citasErrorCargaDato));
            }
        });

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

        public TableLayout tlCvCitaPelu;

        public TextView tvCitaPeluTitFech, tvCitaPeluFech, tvCitaPeluCli, tvCitaPeluMasc, tvCitaPeluServ,
                tvCitaPeluPrec;

        public ViewHolder(View itemView) {
            super(itemView);

            tlCvCitaPelu = (TableLayout) itemView.findViewById(R.id.tlCvCitaPelu);

            tvCitaPeluTitFech = (TextView) itemView.findViewById(R.id.tvCitaPeluTitFech);
            tvCitaPeluFech = (TextView) itemView.findViewById(R.id.tvCitaPeluFech);
            tvCitaPeluCli = (TextView) itemView.findViewById(R.id.tvCitaPeluCli);
            tvCitaPeluMasc = (TextView) itemView.findViewById(R.id.tvCitaPeluMasc);
            tvCitaPeluServ = (TextView) itemView.findViewById(R.id.tvCitaPeluServ);
            tvCitaPeluPrec = (TextView) itemView.findViewById(R.id.tvCitaPeluPrec);

        }

    }

}
