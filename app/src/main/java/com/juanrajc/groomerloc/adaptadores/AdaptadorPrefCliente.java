package com.juanrajc.groomerloc.adaptadores;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.juanrajc.groomerloc.R;

public class AdaptadorPrefCliente extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferencias_cliente);

    }
}
