package com.juanrajc.groomerloc.adaptadores;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.juanrajc.groomerloc.R;
import com.juanrajc.groomerloc.recursos.GlideApp;

import java.util.List;

public class AdaptadorPerros extends RecyclerView.Adapter<AdaptadorPerros.ViewHolder>  {

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
    public AdaptadorPerros(List<String> listaPerros){

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
                .inflate(R.layout.cv_perros, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

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

        /*Listeners de los CardViews.*/
        holder.ivFotoPerroLista.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {



            }
        });
        holder.ibEditPerro.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {



            }
        });
        holder.ibBorraPerro.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                borraPerro(position);

            }
        });

    }

    /**
     * Método que borra el perro seleccionado.
     *
     * @param posicion Posición en el List donde se ecuentra el perro seleccionado (número entero).
     */
    private void borraPerro(final int posicion){

        firestore.collection("clientes").document(usuario.getUid())
                .collection("perros").document(listaPerros.get(posicion))
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                borraFotoPerro(posicion);

                Toast.makeText(contexto, listaPerros.get(posicion)+" "+contexto.getText(R.string.mensajePerroBorradoExito),
                        Toast.LENGTH_SHORT).show();

                //Se elimina el perro del List y se notifica al RecyclerView.
                listaPerros.remove(posicion);
                notifyItemRemoved(posicion);
                notifyItemRangeChanged(posicion, listaPerros.size());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(contexto, listaPerros.get(posicion)+" "+contexto.getText(R.string.mensajePerroNoBorrado),
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Método que borra la foto del perro guardada en Firebase Storage.
     *
     * @param posicion Posición en el List donde se ecuentra el perro seleccionado (número entero).
     */
    private void borraFotoPerro(int posicion){

        FirebaseStorage.getInstance().getReference("fotos/" + usuario.getUid()
                + "/" + listaPerros.get(posicion) + ".jpg").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

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
        public ImageButton ibEditPerro, ibBorraPerro;

        public ViewHolder(View itemView) {
            super(itemView);

            tvNombrePerroLista = (TextView) itemView.findViewById(R.id.tvNombrePerro);
            ivFotoPerroLista = (ImageView) itemView.findViewById(R.id.ivFotoPerro);

            ibEditPerro = (ImageButton) itemView.findViewById(R.id.ibEditPerro);
            ibBorraPerro = (ImageButton) itemView.findViewById(R.id.ibBorraPerro);

        }

    }

}
