package com.juanrajc.groomerloc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class RegistroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
    }

    protected void siguiente(View view){

        startActivity(new Intent(this, RegistroLocActivity.class));

    }

    protected void atras(View view){

        finish();

    }

}
