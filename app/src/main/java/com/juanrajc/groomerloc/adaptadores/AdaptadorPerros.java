package com.juanrajc.groomerloc.adaptadores;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.juanrajc.groomerloc.R;
import com.juanrajc.groomerloc.recursos.GlideApp;

import java.util.List;

public class AdaptadorPerros extends RecyclerView.Adapter<AdaptadorPerros.ViewHolder> {

    //Objeto que contendrá la lista de perros que se van a mostrar.
    private List<String> listaPerros;

    /**
     * Constructor del adaptador.
     *
     * @param listaPerros List con los perros que se van a mostrar.
     */
    public AdaptadorPerros(List<String> listaPerros){

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

        //Se muestra el nombre del perro en el TextView.
        holder.tvNombrePerroLista.setText(listaPerros.get(position));

        /*
        Con Glide, se obtiene la imagen guardada en el Storage de Firebase del perro y se muestra en el ImageView.
            * En "with" se indica el contexto de la vista.
            * En "load" se introduce la referencia de la imagen que se va a motrar.
            * Si el perro no tiene imagen guardada, en "apply" se indica qué imagen se mostrará en sustitución.
            * En "into" se indica el ImageView donde se va a introducir la imagen.
        */
        GlideApp.with(holder.ivFotoPerroLista)
                .load(FirebaseStorage.getInstance().getReference()
                        .child("fotos/"+FirebaseAuth.getInstance().getCurrentUser().getUid() +"/"+listaPerros.get(position)+".jpg"))
                .apply(new RequestOptions().placeholder(R.drawable.icono_mascota).error(R.drawable.icono_mascota))
                .into(holder.ivFotoPerroLista);

    }

    @Override
    public int getItemCount() {

        if(listaPerros!=null) {
            return listaPerros.size();
        }else{
            return 0;
        }

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
