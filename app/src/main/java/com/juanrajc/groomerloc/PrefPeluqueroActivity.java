package com.juanrajc.groomerloc;

import android.app.Activity;
import android.os.Bundle;

import com.juanrajc.groomerloc.adaptadores.AdaptadorPrefPeluquero;

public class PrefPeluqueroActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new AdaptadorPrefPeluquero())
                .commit();

    }

}
