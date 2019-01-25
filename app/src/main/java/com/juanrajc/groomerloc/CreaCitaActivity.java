package com.juanrajc.groomerloc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class CreaCitaActivity extends AppCompatActivity {

    private TextView tvCreaCitaPelu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crea_cita);

        tvCreaCitaPelu = findViewById(R.id.tvCreaCitaPelu);

        tvCreaCitaPelu.setText(getIntent().getStringExtra("nombrePeluquero"));

    }
}
