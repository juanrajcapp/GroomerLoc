package com.juanrajc.groomerloc;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.juanrajc.groomerloc.clasesBD.Cliente;

public class EditCuentaCliActivity extends AppCompatActivity {

    //Objetos de la vista.
    private TextView tvEdCuCliMail, tvEdCuCliNombre, tvEdCuCliTlfn;
    private Button bEdCuCliMail, bEdCuCliNombre, bEdCuCliTlfn, bEdCuCliPw, bEdCuCliEliCu, botonEdCuCliAtras;

    //Objeto del círculo de carga.
    private ProgressBar circuloCargaEdCuCli;

    //Objetos de Firebase (Autenticación y BD Firestore).
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    //Objetos que guardan los datos del cliente cargados.
    private String emailCliente, nombreCliente;
    private long telefonoCliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cuenta_cli);

        //Instancia del círculo de carga.
        circuloCargaEdCuCli = (ProgressBar) findViewById(R.id.circuloCargaEdCuCli);

        //Instancia de los campos de la vista
        tvEdCuCliMail = findViewById(R.id.tvEdCuCliMail);
        tvEdCuCliNombre = findViewById(R.id.tvEdCuCliNombre);
        tvEdCuCliTlfn = findViewById(R.id.tvEdCuCliTlfn);

        //Instancia de los botones de la activity.
        bEdCuCliMail = findViewById(R.id.bEdCuCliMail);
        bEdCuCliNombre = findViewById(R.id.bEdCuCliNombre);
        bEdCuCliTlfn = findViewById(R.id.bEdCuCliTlfn);
        bEdCuCliPw = findViewById(R.id.bEdCuCliPw);
        bEdCuCliEliCu = findViewById(R.id.bEdCuCliEliCu);
        botonEdCuCliAtras = findViewById(R.id.botonEdCuCliAtras);
        /*Inician 'no clicables' los botones de la activity. No se podrán cliquear hasta
        que los datos se hayan cargado.*/
        activaBotones(false);

        //Instancias de la autenticación y la base de datos de Firebase.
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        cargaDatosCliente();

    }

    /**
     * Método que carga los datos del cliente actualmente autenticado.
     */
    private void cargaDatosCliente(){

        //Se visibiliza el círculo de carga.
        circuloCargaEdCuCli.setVisibility(View.VISIBLE);

        //Obtiene los datos del cliente actual desde la BD de Firebase Firestore.
        firestore.collection("clientes").document(auth.getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {

                    //Si existen datos...
                    if (task.getResult().exists()) {

                        Cliente cliente = task.getResult().toObject(Cliente.class);

                        //los guarda en variables de clase...
                        emailCliente = auth.getCurrentUser().getEmail();
                        nombreCliente = cliente.getNombre();
                        telefonoCliente = cliente.getTelefono();

                        //y los muestra en la activity.
                        tvEdCuCliMail.setText(auth.getCurrentUser().getEmail());
                        tvEdCuCliNombre.setText(cliente.getNombre());
                        tvEdCuCliTlfn.setText(String.valueOf(cliente.getTelefono()));

                        //Al terminar la carga de datos, invisibiliza el círculo de carga...
                        circuloCargaEdCuCli.setVisibility(View.INVISIBLE);

                        //y activa los botones.
                        activaBotones(true);

                    }else{
                        Toast.makeText(getApplicationContext(), getString(R.string.mensajeEditCuentaErrorCarga),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.mensajeEditCuentaErrorCarga),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.mensajeEditCuentaErrorCarga),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    /**
     * Método que controla la activación de los botones de la activity.
     *
     * @param boleano Booleano true para activar los botones, y false para desactivarlos.
     */
    private void activaBotones(boolean boleano){

        bEdCuCliMail.setEnabled(boleano);
        bEdCuCliNombre.setEnabled(boleano);
        bEdCuCliTlfn.setEnabled(boleano);
        bEdCuCliPw.setEnabled(boleano);
        bEdCuCliEliCu.setEnabled(boleano);
        botonEdCuCliAtras.setEnabled(boleano);

    }

    /**
     * Método que muestra un AlertDialog donde se pide la contraseña del usuario actual para
     * renovar las credenciales y así poder modificar datos críticos de dicho usuario.
     */
    private void dialogAvisoAutenticacion(){

        //EditText que recoge la contraseña introducida por el usuario.
        final EditText pw = new EditText(this);
        pw.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pw.setTransformationMethod(PasswordTransformationMethod.getInstance());

        //Instancia del AlertDialog y su mensaje.
        AlertDialog.Builder adCambiaPw = new AlertDialog.Builder(this);
        adCambiaPw.setMessage(getString(R.string.alDiEditCuentaAvisoAut));

        //Añade el EditText al AlertDialog.
        adCambiaPw.setView(pw);

        //Setea los botones del AlertDialog y se muestra.
        adCambiaPw.setPositiveButton(getString(R.string.alDiEditCuentaAutenticar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //Renueva credenciales.
                auth.getCurrentUser().reauthenticate(EmailAuthProvider
                        .getCredential(auth.getCurrentUser().getEmail(), pw.getText().toString()))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){

                                    Toast.makeText(getApplicationContext(), getString(R.string.mensajeEditCuentaAutExito),
                                            Toast.LENGTH_SHORT).show();

                                }else{

                                    try {

                                        throw task.getException();

                                    }catch (FirebaseAuthInvalidCredentialsException pwIncorrecto){
                                        Toast.makeText(getApplicationContext(), getString(R.string.mensajeEditCuentaAutPwIncorrecto),
                                                Toast.LENGTH_SHORT).show();
                                    }catch (Exception ex){
                                        Toast.makeText(getApplicationContext(), getString(R.string.mensajeEditCuentaAutFallo),
                                                Toast.LENGTH_SHORT).show();
                                    }

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), getString(R.string.mensajeEditCuentaAutFallo),
                                Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).setNegativeButton(getString(R.string.salir), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Cierra el dialog.
                dialogInterface.dismiss();
            }
        }).show();

    }

    /**
     * Método que cambia el email del usuario actual.
     *
     * @param view
     */
    protected void cambiaEmail(View view){

        //Deactiva los botones.
        activaBotones(false);

        //EditText que recoge el nuevo email introducido por el usuario.
        final EditText email = new EditText(this);
        email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        //Instancia del AlertDialog y su mensaje.
        AlertDialog.Builder adCambiaEmail = new AlertDialog.Builder(this);
        adCambiaEmail.setMessage(getString(R.string.alDiEditCuentaTituloEmail));

        //Añade el EditText al AlertDialog.
        adCambiaEmail.setView(email);

        //Setea los botones del AlertDialog y se muestra.
        adCambiaEmail.setPositiveButton(getString(R.string.botonEditCuentaGuardar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //Comprueba que se ha escrito algo en el campo del email.
                if(email.getText().toString().length()>0) {

                    //Comprueba que el email introducido es válido.
                    if (Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {

                        //Comprueba que el email introducido no es igual al que ya existe.
                        if (!email.getText().toString().equalsIgnoreCase(emailCliente)) {

                            //Actualiza el email del usuario actual.
                            auth.getCurrentUser().updateEmail(email.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                Toast.makeText(getApplicationContext(),
                                                        getString(R.string.mensajeEditCuentaEmailExito),
                                                        Toast.LENGTH_SHORT).show();

                                                //Guarda el nuevo email en la variable de clase...
                                                emailCliente = email.getText().toString();
                                                //y lo muestra en la activity.
                                                tvEdCuCliMail.setText(email.getText().toString());

                                            }else{

                                                try{

                                                    throw task.getException();

                                                }catch (FirebaseAuthRecentLoginRequiredException necesitaRelogueo) {
                                                    dialogAvisoAutenticacion();
                                                }catch(FirebaseAuthUserCollisionException emailExistente){

                                                    Toast.makeText(getApplicationContext(),
                                                            email.getText().toString() +" "+
                                                                    getString(R.string.mensajeEditCuentaExisteEmail),
                                                            Toast.LENGTH_SHORT).show();

                                                }catch (Exception ex){

                                                    Toast.makeText(getApplicationContext(),
                                                            getString(R.string.mensajeEditCuentaErrorMail),
                                                            Toast.LENGTH_SHORT).show();

                                                }

                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(),
                                            getString(R.string.mensajeEditCuentaErrorMail),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.mensajeEditCuentaMismoEmail),
                                    Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.mensajeEmail),
                                Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.mensajeEmail),
                            Toast.LENGTH_SHORT).show();
                }

            }
        }).setNegativeButton(getString(R.string.salir), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Cierra el dialog.
                dialogInterface.dismiss();
            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //Activa los botones al salir del AlertDialog.
                activaBotones(true);
            }
        }).show();

    }

    /**
     * Método que cambia el nombre del usuario actual.
     *
     * @param view
     */
    protected void cambiaNombre(View view){

        //Deactiva los botones.
        activaBotones(false);

        //EditText que recoge el nuevo nombre introducido por el usuario.
        final EditText nombre = new EditText(this);
        nombre.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);

        //Instancia del AlertDialog y su mensaje.
        AlertDialog.Builder adCambiaNombre = new AlertDialog.Builder(this);
        adCambiaNombre.setMessage(getString(R.string.alDiEditCuentaTituloNombre));

        //Añade el EditText al AlertDialog.
        adCambiaNombre.setView(nombre);

        //Setea los botones del AlertDialog y se muestra.
        adCambiaNombre.setPositiveButton(getString(R.string.botonEditCuentaGuardar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //Comprueba que se ha escrito algo en el campo del nombre.
                if(nombre.getText().toString().length()>0){

                    //Comprueba que el nombre introducido no es igual que el que ya existe.
                    if(!nombre.getText().toString().equals(nombreCliente)){

                        //Actualiza el nombre en la BD del usuario en Firestore.
                        firestore.collection("clientes")
                                .document(auth.getCurrentUser().getUid())
                                .update("nombre", nombre.getText().toString())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        //Actualiza también el nombre en el perfil del usuario actual.
                                        auth.getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().
                                                setDisplayName(nombre.getText().toString()).build());

                                        Toast.makeText(getApplicationContext(),
                                                getString(R.string.mensajeEditCuentaNombreExito),
                                                Toast.LENGTH_SHORT).show();

                                        //Guarda el nuevo nombre en la variable de clase...
                                        nombreCliente = nombre.getText().toString();
                                        //y lo muestra en la activity.
                                        tvEdCuCliNombre.setText(nombre.getText().toString());

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.mensajeEditCuentaErrorNombre),
                                        Toast.LENGTH_SHORT).show();

                            }
                        });

                    }else{
                        Toast.makeText(getApplicationContext(), getString(R.string.mensajeEditCuentaMismoNombre),
                                Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.mensajeEditCuentaNada),
                            Toast.LENGTH_SHORT).show();
                }

            }
        }).setNegativeButton(getString(R.string.salir), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Cierra el dialog.
                dialogInterface.dismiss();
            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //Activa los botones al salir del AlertDialog.
                activaBotones(true);
            }
        }).show();

    }

    /**
     * Método que cambia el teléfono del usuario actual.
     *
     * @param view
     */
    protected void cambiaTlfn(View view){

        //Deactiva los botones.
        activaBotones(false);

        //EditText que recoge el nuevo teléfono introducido por el usuario.
        final EditText tlfn = new EditText(this);
        tlfn.setInputType(InputType.TYPE_CLASS_NUMBER);

        //Instancia del AlertDialog y su mensaje.
        AlertDialog.Builder adCambiaTlfn = new AlertDialog.Builder(this);
        adCambiaTlfn.setMessage(getString(R.string.alDiEditCuentaTituloTlfn));

        //Añade el EditText al AlertDialog.
        adCambiaTlfn.setView(tlfn);

        //Setea los botones del AlertDialog y se muestra.
        adCambiaTlfn.setPositiveButton(getString(R.string.botonEditCuentaGuardar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //Comprueba que se ha escrito algo en el campo del teléfono.
                if(tlfn.getText().toString().length()>0){

                    //Comprueba que el teléfono introducido no es igual que el que ya existe.
                    if(Long.parseLong(tlfn.getText().toString())!=telefonoCliente){

                        //Actualiza el teléfono en la BD del usuario en Firestore.
                        firestore.collection("clientes")
                                .document(auth.getCurrentUser().getUid())
                                .update("telefono", Long.parseLong(tlfn.getText().toString()))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(getApplicationContext(),
                                                getString(R.string.mensajeEditCuentaTlfnExito),
                                                Toast.LENGTH_SHORT).show();

                                        //Guarda el nuevo teléfono en la variable de clase...
                                        telefonoCliente = Long.parseLong(tlfn.getText().toString());
                                        //y lo muestra en la activity.
                                        tvEdCuCliTlfn.setText(tlfn.getText().toString());

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.mensajeEditCuentaErrorTlfn),
                                        Toast.LENGTH_SHORT).show();

                            }
                        });

                    }else{
                        Toast.makeText(getApplicationContext(), getString(R.string.mensajeEditCuentaMismoTlfn),
                                Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.mensajeEditCuentaNada),
                            Toast.LENGTH_SHORT).show();
                }

            }
        }).setNegativeButton(getString(R.string.salir), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Cierra el dialog.
                dialogInterface.dismiss();
            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //Activa los botones al salir del AlertDialog.
                activaBotones(true);
            }
        }).show();

    }

    /**
     * Método que cambia la contraseña del usuario actual.
     *
     * @param view
     */
    protected void cambiaPw(View view){

        //Deactiva los botones.
        activaBotones(false);

        //LinearLayout vertical que contendrán los EditTexts de la contraseña.
        LinearLayout llDialog = new LinearLayout(this);
        llDialog.setOrientation(LinearLayout.VERTICAL);

        //EditTexts que recogen la nueva contraseña introducida por el usuario.
        final EditText pw = new EditText(this);
        pw.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pw.setTransformationMethod(PasswordTransformationMethod.getInstance());
        final EditText pw2 = new EditText(this);
        pw2.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pw2.setTransformationMethod(PasswordTransformationMethod.getInstance());

        //Añaden los EditTexts al LinearLayout.
        llDialog.addView(pw);
        llDialog.addView(pw2);

        //Instancia del AlertDialog y su mensaje.
        AlertDialog.Builder adCambiaPw = new AlertDialog.Builder(this);
        adCambiaPw.setMessage(getString(R.string.alDiEditCuentaTituloPw));

        //Añade el LinearLayout al AlertDialog.
        adCambiaPw.setView(llDialog);

        //Setea los botones del AlertDialog y se muestra.
        adCambiaPw.setPositiveButton(getString(R.string.botonEditCuentaGuardar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //Comprueba que se ha escrito algo en los campos de la contraseña.
                if(pw.getText().toString().length()>0 && pw2.getText().toString().length()>0){

                    //Comprueba que la longitud de las contraseñas sea de al menos 6 caracteres.
                    if(pw.getText().toString().length()>=6 && pw2.getText().toString().length()>=6){

                        //Comprueba que las contraseñas introducidas sean iguales entre sí.
                        if(pw.getText().toString().equals(pw2.getText().toString())){

                            //Actualiza la contraseña del usuario actual.
                            auth.getCurrentUser().updatePassword(pw.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                Toast.makeText(getApplicationContext(),
                                                        getString(R.string.mensajeEditCuentaExitoPw),
                                                        Toast.LENGTH_SHORT).show();

                                            }else{

                                                try{

                                                    throw task.getException();

                                                }catch (FirebaseAuthRecentLoginRequiredException necesitaRelogueo) {
                                                    dialogAvisoAutenticacion();
                                                }catch (Exception ex){

                                                    Toast.makeText(getApplicationContext(),
                                                            getString(R.string.mensajeEditCuentaErrorPw),
                                                            Toast.LENGTH_SHORT).show();

                                                }

                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(),
                                            getString(R.string.mensajeEditCuentaErrorPw),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                        }else{
                            Toast.makeText(getApplicationContext(), getString(R.string.mensajePw),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }else{
                        Toast.makeText(getApplicationContext(), getString(R.string.mensajePw2),
                                Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.mensajeEditCuentaPwVacio),
                            Toast.LENGTH_SHORT).show();
                }

            }
        }).setNegativeButton(getString(R.string.salir), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Cierra el dialog.
                dialogInterface.dismiss();
            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //Activa los botones al salir del AlertDialog.
                activaBotones(true);
            }
        }).show();

    }

    /**
     * Método que elimina la cuenta del usuario actual.
     *
     * @param view
     */
    protected void eliminaCuenta(View view){

        //Deactiva los botones.
        activaBotones(false);

        //Inicia un AlertDialog que confirmará o no la eliminación de la cuenta.
        new AlertDialog.Builder(this).setMessage(getString(R.string.alDiEditCuentaBorrar))
                .setPositiveButton(getString(R.string.si), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //Guarda la ID del usuario actual en una variable.
                        final String idUsuario = auth.getCurrentUser().getUid();

                        //Elimina el usuario actual...
                        auth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){

                                    //y sus datos en Firestore.
                                    firestore.collection("clientes").document(idUsuario).delete();

                                    Toast.makeText(getApplicationContext(),
                                            getText(R.string.mensajeEditCuentaBorrada)+" "+emailCliente+" "+
                                            getString(R.string.mensajeEditCuentaBorrada2),
                                            Toast.LENGTH_SHORT).show();

                                    //Finalmente cierra la activity.
                                    finish();

                                }else{

                                    try{

                                        throw task.getException();

                                    }catch (FirebaseAuthRecentLoginRequiredException necesitaRelogueo) {
                                        dialogAvisoAutenticacion();
                                    }catch (Exception ex){

                                        Toast.makeText(getApplicationContext(),
                                                getString(R.string.mensajeEditCuentaErrorBorrar),
                                                Toast.LENGTH_SHORT).show();

                                    }

                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.mensajeEditCuentaErrorBorrar),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Cierra el dialog.
                dialogInterface.dismiss();
            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //Activa los botones al salir del AlertDialog.
                activaBotones(true);
            }
        }).show();

    }

    /**
     * Método que controla el retroceso a la activity anterior.
     *
     * @param view
     */
    protected void atras(View view){

        //Se desactivan los botones...
        activaBotones(false);

        //y se cierra la activity.
        finish();

    }

}
