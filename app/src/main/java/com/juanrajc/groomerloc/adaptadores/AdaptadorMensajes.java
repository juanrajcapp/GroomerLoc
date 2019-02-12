package com.juanrajc.groomerloc.adaptadores;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.juanrajc.groomerloc.R;
import com.juanrajc.groomerloc.clasesBD.Mensaje;

import java.text.SimpleDateFormat;
import java.util.List;

public class AdaptadorMensajes extends ArrayAdapter<Mensaje> {

    //Objetos de los elementos del mensaje.
    private TextView mensajeUsuario, mensajeFecha, mensajeTexto;

    public AdaptadorMensajes(@NonNull Context context, List<Mensaje> mensajes) {
        super(context, 0, mensajes);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null){
            convertView = ((FragmentActivity)getContext()).getLayoutInflater()
                    .inflate(R.layout.mensaje, parent, false);
        }

        //Instancias de los elementos del mensaje.
        mensajeUsuario = convertView.findViewById(R.id.mensajeUsuario);
        mensajeFecha = convertView.findViewById(R.id.mensajeFecha);
        mensajeTexto = convertView.findViewById(R.id.mensajeTexto);

        Mensaje mensaje = getItem(position);

        //Introduce los valores obtenidos en los elementos del mensaje.
        mensajeUsuario.setText(mensaje.getUsuario());
        mensajeFecha.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(mensaje.getFecha()));
        mensajeTexto.setText(mensaje.getTexto());

        return convertView;

    }
}
