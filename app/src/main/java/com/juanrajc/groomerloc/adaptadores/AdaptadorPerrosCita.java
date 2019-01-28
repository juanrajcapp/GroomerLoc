package com.juanrajc.groomerloc.adaptadores;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.juanrajc.groomerloc.PerrosCitaActivity;
import com.juanrajc.groomerloc.R;
import com.juanrajc.groomerloc.clasesBD.Perro;
import com.juanrajc.groomerloc.recursos.GlideApp;

import java.util.List;

import static android.app.Activity.RESULT_OK;

public class AdaptadorPerrosCita extends RecyclerView.Adapter<AdaptadorPerrosCita.ViewHolder>  {

    //Objeto del contexto de la aplicación.
    Context contexto;

    //Objeto del usuario actual y de la BD Firestore.
    private FirebaseUser usuario;
    private FirebaseFirestore firestore;

    //Objeto que contendrá la lista de perros que se van a mostrar.
    private List<String> listaPerros;

    /**
     * Constructor del adaptador.
     *
     * @param listaPerros List con los perros que se van a mostrar.
     */
    public AdaptadorPerrosCita(List<String> listaPerros){

        //Instancia del usuario actual y de la base de datos Firestore.
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        this.listaPerros=listaPerros;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        contexto = parent.getContext();

        return new ViewHolder(LayoutInflater.from(contexto)
                .inflate(R.layout.cv_perros_cita, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        //Se muestra el nombre del perro en el TextView.
        holder.tvNombrePerroLista.setText(listaPerros.get(position));

        /*
        Con Glide, se obtiene la imagen guardada en el Storage de Firebase del perro y se muestra en el ImageView.
            * En "with" se indica el contexto de la vista.
            * En "load" se introduce la referencia de la imagen que se va a motrar.
            * Si el perro no tiene imagen guardada, en "apply" se indica qué imagen se mostrará en sustitución.
            * En "into" se indica el ImageView donde se va a introducir la imagen.
        */
        GlideApp.with(contexto)
                .load(FirebaseStorage.getInstance().getReference()
                        .child("fotos/"+FirebaseAuth.getInstance().getCurrentUser().getUid() +"/"+listaPerros.get(position)+".jpg"))
                .apply(new RequestOptions().placeholder(R.drawable.icono_mascota).error(R.drawable.icono_mascota))
                .into(holder.ivFotoPerroLista);

        /*Listeners de los CardViews.*/
        holder.tvNombrePerroLista.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                /*
                Se desactiva la posibilidad de pulsación de los botones para evitar
                múltiples pulsaciones accidentales.
                */
                holder.tvNombrePerroLista.setClickable(false);
                holder.ivFotoPerroLista.setClickable(false);

                //Devuelve el nombre del perro seleccionado...
                ((PerrosCitaActivity) contexto).setResult(RESULT_OK, new Intent()
                        .putExtra("nombrePerro", listaPerros.get(position)));

                //y cierra la activity.
                ((PerrosCitaActivity) contexto).finish();

            }
        });

        holder.ivFotoPerroLista.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                /*
                Se desactiva la posibilidad de pulsación de los botones para evitar
                múltiples pulsaciones accidentales.
                */
                holder.ivFotoPerroLista.setClickable(false);
                holder.tvNombrePerroLista.setClickable(false);

                //Devuelve el nombre del perro seleccionado...
                ((PerrosCitaActivity) contexto).setResult(RESULT_OK, new Intent()
                        .putExtra("nombrePerro", listaPerros.get(position)));

                //y cierra la activity.
                ((PerrosCitaActivity) contexto).finish();

            }
        });

    }

    @Override
    public int getItemCount() {

        if(listaPerros!=null) {
            return listaPerros.size();
        }else{
            return 0;
        }

    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView tvNombrePerroLista;
        public ImageView ivFotoPerroLista;

        public ViewHolder(View itemView) {
            super(itemView);

            tvNombrePerroLista = (TextView) itemView.findViewById(R.id.tvNombrePerroCita);
            ivFotoPerroLista = (ImageView) itemView.findViewById(R.id.ivFotoPerroCita);

        }

    }

}