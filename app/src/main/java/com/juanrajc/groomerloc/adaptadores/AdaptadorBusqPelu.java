package com.juanrajc.groomerloc.adaptadores;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.juanrajc.groomerloc.BusqPeluActivity;
import com.juanrajc.groomerloc.R;
import com.juanrajc.groomerloc.recursos.GlideApp;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorBusqPelu extends RecyclerView.Adapter<AdaptadorBusqPelu.ViewHolder>  {

    //Objeto del contexto de la aplicación.
    Context contexto;

    //Objeto que contendrá la lista de peluqueros que se van a mostrar.
    private List<String> listaIdPeluqueros, listaNombresPeluqueros, listaDirPeluqueros;

    public AdaptadorBusqPelu(ArrayList<String> listaIdPeluqueros, ArrayList<String> listaNombresPeluqueros, ArrayList<String> listaDirPeluqueros){

        this.listaIdPeluqueros = listaIdPeluqueros;
        this.listaNombresPeluqueros = listaNombresPeluqueros;
        this.listaDirPeluqueros = listaDirPeluqueros;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        contexto = parent.getContext();

        return new ViewHolder(LayoutInflater.from(contexto)
                .inflate(R.layout.cv_busqpelu, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        //Se muestra el nombre del peluquero en el TextView.
        holder.tvNombrePeluLista.setText(listaNombresPeluqueros.get(position));

        /*
        Con Glide, se obtiene la imagen guardada en el Storage de Firebase del peluquero y se muestra en el ImageView.
            * En "with" se indica el contexto de la vista.
            * En "load" se introduce la referencia de la imagen que se va a motrar.
            * Si el perro no tiene imagen guardada, en "apply" se indica qué imagen se mostrará en sustitución.
            * En "into" se indica el ImageView donde se va a introducir la imagen.
        */
        GlideApp.with(holder.ivFotoPeluLista)
                .load(FirebaseStorage.getInstance().getReference()
                        .child("fotos/"+listaIdPeluqueros.get(position)+"/"+listaNombresPeluqueros.get(position)+".jpg"))
                .apply(new RequestOptions().placeholder(R.drawable.icono_loc_persona).error(R.drawable.icono_loc_persona))
                .into(holder.ivFotoPeluLista);

        holder.tvDirPeluLista.setText(listaDirPeluqueros.get(position));

        /*Listeners de los CardViews.*/
        holder.tvNombrePeluLista.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                ((BusqPeluActivity) contexto).devuelvePeluqueroSeleccionado(listaIdPeluqueros.get(position));

            }
        });
        holder.ivFotoPeluLista.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                ((BusqPeluActivity) contexto).devuelvePeluqueroSeleccionado(listaIdPeluqueros.get(position));

            }
        });
        holder.tvDirPeluLista.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                ((BusqPeluActivity) contexto).devuelvePeluqueroSeleccionado(listaIdPeluqueros.get(position));

            }
        });

    }

    @Override
    public int getItemCount() {

        if(listaIdPeluqueros!=null) {
            return listaIdPeluqueros.size();
        }else{
            return 0;
        }

    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView tvNombrePeluLista, tvDirPeluLista;
        public ImageView ivFotoPeluLista;

        public ViewHolder(View itemView) {
            super(itemView);

            tvNombrePeluLista = (TextView) itemView.findViewById(R.id.tvNombrePelu);
            ivFotoPeluLista = (ImageView) itemView.findViewById(R.id.ivFotoPelu);
            tvDirPeluLista = (TextView) itemView.findViewById(R.id.tvDirPelu);

        }

    }

}
