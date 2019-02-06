package com.juanrajc.groomerloc.fragmentsCita;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.juanrajc.groomerloc.CitaPeluActivity;
import com.juanrajc.groomerloc.R;

public class FragChatCitaPelu extends Fragment {

    private String idClita;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_chat_cita_pelu, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        idClita = ((CitaPeluActivity)getContext()).getIdCita();

    }
}
