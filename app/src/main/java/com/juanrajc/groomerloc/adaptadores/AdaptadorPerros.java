package com.juanrajc.groomerloc.adaptadores;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.juanrajc.groomerloc.PerrosActivity;
import com.juanrajc.groomerloc.R;
import com.juanrajc.groomerloc.RegPerroActivity;
import com.juanrajc.groomerloc.clasesBD.Perro;
import com.juanrajc.groomerloc.recursos.GlideApp;

import java.util.Date;
import java.util.List;

public class AdaptadorPerros extends RecyclerView.Adapter<AdaptadorPerros.ViewHolder>  {

    //Objeto del contexto de la aplicación.
    private Context contexto;

    //Objeto del usuario actual y de la BD Firestore.
    private FirebaseUser usuario;
    private FirebaseFirestore firestore;

    //Objetos que contendrá las IDs y datos de los perros que se van a mostrar.
    private List<String> listaIdsPerros;
    private List<Perro> listaObjPerros;

    /**
     * Constructor del adaptador.
     *
     * @param listaIdsPerros Cadenas con las IDs de los perros.
     * @param listaObjPerros Objetos con los datos de los perros.
     */
    public AdaptadorPerros(List<String> listaIdsPerros, List<Perro> listaObjPerros){

        //Instancia del usuario actual y de la base de datos Firestore.
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        this.listaIdsPerros=listaIdsPerros;
        this.listaObjPerros=listaObjPerros;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        contexto = parent.getContext();

        return new ViewHolder(LayoutInflater.from(contexto)
                .inflate(R.layout.cv_perros, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        //Se muestra el nombre del perro en el TextView.
        holder.tvNombrePerroLista.setText(listaObjPerros.get(position).getNombre());

        /*
        Con Glide, se obtiene la imagen guardada en el Storage de Firebase del perro y se muestra en el ImageView.
            * En "with" se indica el contexto de la vista.
            * En "load" se introduce la referencia de la imagen que se va a motrar.
            * Si el perro no tiene imagen guardada, en "apply" se indica qué imagen se mostrará en sustitución.
            * En "into" se indica el ImageView donde se va a introducir la imagen.
        */
        GlideApp.with(contexto)
                .load(FirebaseStorage.getInstance().getReference()
                        .child("clientes/"+usuario.getUid()+"/perros/"+listaIdsPerros.get(position)
                                +"/fotos/"+listaIdsPerros.get(position)
                                +" "+listaObjPerros.get(position).getFechaFoto()+".jpg"))
                .apply(new RequestOptions().placeholder(R.drawable.icono_mascota)
                        .error(R.drawable.icono_mascota))
                .signature(new ObjectKey(listaIdsPerros.get(position)+new Date().getTime()))
                .into(holder.ivFotoPerroLista);

        /*Listeners de los CardViews.*/
        holder.ivFotoPerroLista.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                /*
                Se desactiva la posibilidad de pulsación de los botones para evitar
                múltiples pulsaciones accidentales.
                */
                holder.ivFotoPerroLista.setClickable(false);
                holder.ibEditPerro.setClickable(false);
                holder.ibBorraPerro.setClickable(false);

                muestraDatosPerro(position, formateaDatosPerro(listaObjPerros.get(position)), holder);

            }
        });
        holder.ibEditPerro.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                /*
                Se desactiva la posibilidad de pulsación de los botones para evitar
                múltiples pulsaciones accidentales.
                */
                holder.ibEditPerro.setClickable(false);
                holder.ivFotoPerroLista.setClickable(false);
                holder.ibBorraPerro.setClickable(false);

                //Inicia la activity de registro de perros, pasándole la ID del perro a editar.
                ((PerrosActivity) contexto).startActivity(new Intent(contexto, RegPerroActivity.class)
                        .putExtra("idPerro", listaIdsPerros.get(position)));

            }
        });
        holder.ibBorraPerro.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                /*
                Se desactiva la posibilidad de pulsación de los botones para evitar
                múltiples pulsaciones accidentales.
                */
                holder.ibBorraPerro.setClickable(false);
                holder.ivFotoPerroLista.setClickable(false);
                holder.ibEditPerro.setClickable(false);

                //Dialogo de alerta que pregunta si se desa borrar el perro seleccionado.
                new AlertDialog.Builder(contexto, R.style.AppTheme_Dialog)
                        .setTitle(contexto.getText(R.string.dialogBorraPerro)+" "+listaObjPerros.get(position).getNombre()+"?")
                        .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        borraPerro(position, holder);

                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //Cierra el dialog.
                        dialogInterface.cancel();

                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {

                        /*
                        Si se cancela el borrado o se sale del dialog, se vuelve a activar la
                        posibilidad de pulsación de los botones.
                        */
                        holder.ibBorraPerro.setClickable(true);
                        holder.ivFotoPerroLista.setClickable(true);
                        holder.ibEditPerro.setClickable(true);

                    }
                }).show();

            }
        });

    }

    /**
     * Método que borra el perro seleccionado.
     *
     * @param posicion Posición en el List donde se ecuentra el perro seleccionado (número entero).
     * @param holder ViewHolder necesario para el manejo de los elementos de cada CardView.
     */
    private void borraPerro(final int posicion, final ViewHolder holder){

        firestore.collection("clientes").document(usuario.getUid())
                .collection("perros").document(listaIdsPerros.get(posicion))
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                borraFotoPerro(posicion);

                Toast.makeText(contexto, listaObjPerros.get(posicion).getNombre()+" "+contexto.getText(R.string.mensajePerroBorradoExito),
                        Toast.LENGTH_SHORT).show();

                //Si se borra con éxito, se elimina el perro de los List y se notifica al RecyclerView.
                listaIdsPerros.remove(posicion);
                listaObjPerros.remove(posicion);
                notifyItemRemoved(posicion);
                notifyItemRangeChanged(posicion, listaIdsPerros.size());
                notifyItemRangeChanged(posicion, listaObjPerros.size());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(contexto, listaObjPerros.get(posicion).getNombre()+" "+contexto.getText(R.string.mensajePerroNoBorrado),
                        Toast.LENGTH_SHORT).show();

                //Si el borrado falla, se vuelve a activar la posibilidad de pulsación de los botones.
                holder.ibBorraPerro.setClickable(true);
                holder.ivFotoPerroLista.setClickable(true);
                holder.ibEditPerro.setClickable(true);

            }
        });

    }

    /**
     * Método que borra la foto del perro guardada en Firebase Storage.
     *
     * @param posicion Posición en el List donde se ecuentra el perro seleccionado (número entero).
     */
    private void borraFotoPerro(int posicion){

        FirebaseStorage.getInstance().getReference("clientes/"+usuario.getUid()
                +"/perros/"+listaIdsPerros.get(posicion)+"/fotos/"+listaIdsPerros.get(posicion)
                +" "+listaObjPerros.get(posicion).getFechaFoto()+".jpg").delete();

    }

    /**
     * Método que formatea los datos de un perro guardado en Firestore.
     *
     * @param perro Objeto con los datos del perro.
     *
     * @return Cadena formateada con los datos listos para ser mostrados.
     */
    private String formateaDatosPerro(Perro perro){

        //Guarda los datos en cadenas.
        String raza=perro.getRaza(), sexo=perro.getSexo(), comentario=perro.getComentario(), peso=String.valueOf(perro.getPeso());

        StringBuffer sb=new StringBuffer();

        if(raza.length()>0){
            sb.append("Raza: "+raza+"\n\n");
        }
        if(sexo.length()>0){

            if(sexo.equals("XY")){
                sb.append("Sexo: "+"Macho"+"\n\n");
            }else if(sexo.equals("XX")){
                sb.append("Sexo: "+"Hembra"+"\n\n");
            }

        }
        if(peso.length()>0){
            sb.append("Peso: "+peso+" Kg\n\n");
        }
        if(comentario.length()>0){
            sb.append("Comentario: "+comentario);
        }

        return sb.toString();

    }

    /**
     * Método que muestra, mediante un AlertDialog, los datos del perro seleccionado.
     *
     * @param posicion Posición en el List donde se ecuentra el perro seleccionado (número entero).
     * @param datos Cadena formateada con los datos listos para ser mostrados.
     * @param holder ViewHolder necesario para el manejo de los elementos de cada CardView.
     */
    private void muestraDatosPerro(int posicion, String datos, ViewHolder holder){

        //Crea un AlertDialog con el estilo especificado.
        AlertDialog.Builder adPerro = new AlertDialog.Builder(contexto, R.style.AppTheme_Dialog);

        //Crea un TV para personalizar el título del dialog.
        TextView tituloDialog = new TextView(contexto);
        tituloDialog.setText(listaObjPerros.get(posicion).getNombre());
        tituloDialog.setGravity(Gravity.CENTER);
        tituloDialog.setBackgroundColor(Color.GRAY);
        tituloDialog.setTextColor(Color.WHITE);
        tituloDialog.setPadding(8, 8, 8, 8);
        tituloDialog.setTextSize(20);

        //Añade el TV al dialog.
        adPerro.setCustomTitle(tituloDialog);

        //Añade la cadena obtenida al mensaje del AD.
        adPerro.setMessage(datos);

        //Añade un botón para salir del AD. Finalmente lo muestra.
        adPerro.setPositiveButton(contexto.getText(R.string.salir), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        }).show();

        //Una vez mostrado el dialog, se vuelve a activar la posibilidad de pulsación de los botones.
        holder.ivFotoPerroLista.setClickable(true);
        holder.ibEditPerro.setClickable(true);
        holder.ibBorraPerro.setClickable(true);

    }

    @Override
    public int getItemCount() {

        if(listaIdsPerros!=null) {
            return listaIdsPerros.size();
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
