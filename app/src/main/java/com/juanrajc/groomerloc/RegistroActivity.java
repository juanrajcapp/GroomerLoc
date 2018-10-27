package com.juanrajc.groomerloc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.regex.Pattern;

public class RegistroActivity extends AppCompatActivity {

    RadioButton rbCliente, rbPeluquero;
    EditText etRegEmail, etRegPw, etRegPw2, etRegNombre, etRegTlfn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        rbCliente=(RadioButton) findViewById(R.id.rbCliente);
        rbPeluquero=(RadioButton) findViewById(R.id.rbPeluquero);

        etRegEmail=(EditText) findViewById(R.id.etRegMail);
        etRegPw=(EditText) findViewById(R.id.etRegPw);
        etRegPw2=(EditText) findViewById(R.id.etRegPw2);
        etRegNombre=(EditText) findViewById(R.id.etRegNombre);
        etRegTlfn=(EditText) findViewById(R.id.etRegTlfn);

    }

    protected boolean compruebaCampos(){

        if(rbCliente.isChecked() || rbPeluquero.isChecked()){

            if(etRegEmail.getText().toString().length()>0 && etRegPw.getText().toString().length()>0 &&
                    etRegPw2.getText().toString().length()>0 && etRegNombre.getText().toString().length()>0 &&
                    etRegTlfn.getText().toString().length()>0){

                if(validarEmail(etRegEmail.getText().toString())){

                    if(etRegPw.getText().toString().equals(etRegPw2.getText().toString())){

                        return true;

                    }else{
                        Toast.makeText(this, getString(R.string.mensajePw), Toast.LENGTH_SHORT).show();
                        etRegPw.setText("");
                        etRegPw2.setText("");
                    }

                }else{
                    Toast.makeText(this, getString(R.string.mensajeEmail), Toast.LENGTH_SHORT).show();
                    etRegEmail.setText("");
                }

            }else{
                Toast.makeText(this, getString(R.string.mensajeCampos), Toast.LENGTH_SHORT).show();
            }

        }else{
            Toast.makeText(this, getString(R.string.mensajeRol), Toast.LENGTH_SHORT).show();
        }

        return false;

    }

    private boolean validarEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    protected void siguiente(View view){

        if(compruebaCampos()){
            startActivity(new Intent(this, RegistroLocActivity.class));
        }

    }

    protected void atras(View view){

        finish();

    }

}
