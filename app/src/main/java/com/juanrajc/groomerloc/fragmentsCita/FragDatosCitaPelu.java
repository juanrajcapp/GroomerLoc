package com.juanrajc.groomerloc.fragmentsCita;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.juanrajc.groomerloc.CitaPeluActivity;
import com.juanrajc.groomerloc.R;

public class FragDatosCitaPelu extends Fragment {

    private TextView tvCitaPeluFechCrea, tvCitaPeluFech, tvCitaPeluServ, tvCitaPeluPrecio,
            tvCitaPeluCli, tvCitaPeluCliTlfn, tvCitaPeluPerro, tvCitaPeluRaza, tvCitaPeluSexo,
            tvCitaPeluPeso, tvCitaPeluComent;

    private ImageView ivCitaPeluPerro;

    private String idClita;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_datos_cita_pelu, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

        ivCitaPeluPerro = getActivity().findViewById(R.id.ivCitaPeluPerro);

        idClita = ((CitaPeluActivity)getContext()).getIdCita();

        obtieneCita();

    }

    private void obtieneCita(){



    }


}
