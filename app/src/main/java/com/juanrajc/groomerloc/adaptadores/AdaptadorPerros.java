package com.juanrajc.groomerloc.adaptadores;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.juanrajc.groomerloc.R;
import com.juanrajc.groomerloc.recursos.CardPerro;

import java.util.List;

public class AdaptadorPerros extends RecyclerView.Adapter<AdaptadorPerros.ViewHolder> {

    private List<CardPerro> listaPerros;

    public AdaptadorPerros(List<CardPerro> listaPerros){

        this.listaPerros=listaPerros;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cv_perros, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.tvNombrePerroLista.setText(listaPerros.get(position).getNombrePerro());
        holder.ivFotoPerroLista.setImageURI(listaPerros.get(position).getFotoPerro());

    }

    @Override
    public int getItemCount() {
        return listaPerros.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvNombrePerroLista;
        public ImageView ivFotoPerroLista;

        public ViewHolder(View itemView) {
            super(itemView);

            tvNombrePerroLista = (TextView) itemView.findViewById(R.id.tvNombrePerro);
            ivFotoPerroLista = (ImageView) itemView.findViewById(R.id.ivFotoPerro);

        }
    }

}
