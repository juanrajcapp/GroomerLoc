package com.juanrajc.groomerloc;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
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

import com.google.android.gms.maps.model.LatLng;
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
import com.juanrajc.groomerloc.clasesBD.Peluquero;

import java.io.IOException;
import java.util.List;

public class EditCuentaPeluActivity extends AppCompatActivity {

    //Constantes con los posibles resultados devueltos a la activity actual.
    private static final int REQUEST_LOC=1;

    //Objetos de la vista.
    private TextView tvEdCuPeluMail, tvEdCuPeluNombre, tvEdCuPeluTlfn, tvEdCuPeluLoc, tvEdCuPeluLocExtra;
    private Button bEdCuPeluMail, bEdCuPeluNombre, bEdCuPeluTlfn, bEdCuPeluPw, bEdCuPeluLoc, bEdCuPeluEliCu, botonEdCuPeluAtras;

    //Objeto del círculo de carga.
    private ProgressBar circuloCargaEdCuPelu;

    //Objetos de Firebase (Autenticación y BD Firestore).
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    //Geocoder para la traducción de coordenadas en direciones y viceversa.
    private Geocoder gc;

    //Objetos que guardan los datos del peluquero cargados.
    private String emailPeluquero, nombrePeluquero;
    private long telefonoPeluquero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cuenta_pelu);

        //Instancia del círculo de carga.
        circuloCargaEdCuPelu = (ProgressBar) findViewById(R.id.circuloCargaEdCuPelu);

        //Instancia de los campos de la vista
        tvEdCuPeluMail = findViewById(R.id.tvEdCuPeluMail);
        tvEdCuPeluNombre = findViewById(R.id.tvEdCuPeluNombre);
        tvEdCuPeluTlfn = findViewById(R.id.tvEdCuPeluTlfn);
        tvEdCuPeluLoc = findViewById(R.id.tvEdCuPeluLoc);
        tvEdCuPeluLocExtra = findViewById(R.id.tvEdCuPeluLocExtra);

        //Instancia de los botones de la activity.
        bEdCuPeluMail = findViewById(R.id.bEdCuPeluMail);
        bEdCuPeluNombre = findViewById(R.id.bEdCuPeluNombre);
        bEdCuPeluTlfn = findViewById(R.id.bEdCuPeluTlfn);
        bEdCuPeluLoc = findViewById(R.id.bEdCuPeluLoc);
        bEdCuPeluPw = findViewById(R.id.bEdCuPeluPw);
        bEdCuPeluEliCu = findViewById(R.id.bEdCuPeluEliCu);
        botonEdCuPeluAtras = findViewById(R.id.botonEdCuPeluAtras);
        /*Inician 'no clicables' los botones de la activity. No se podrán cliquear hasta
        que los datos se hayan cargado.*/
        activaBotones(false);

        //Instancias de la autenticación y la base de datos de Firebase.
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        //Instancia del Geocoder.
        gc=new Geocoder(this);

        cargaDatosPeluquero();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Si el resultado devuelto a la activity es satisfactoria...
        if(resultCode==RESULT_OK) {

            //Comprueba mediante un switch el tipo de resultado devuelto.
            switch (requestCode) {

                case REQUEST_LOC:

                    //Obtiene los extras...
                    Bundle locActualizada = data.getExtras();

                    //y los muestra en los TextView correspondientes.
                    tvEdCuPeluLoc.setText(locActualizada.getString("loc"));
                    tvEdCuPeluLocExtra.setText(locActualizada.getString("locExtra"));

            }
        }

    }

    /**
     * Método que carga los datos del peluquero actualmente autenticado.
     */
    private void cargaDatosPeluquero(){

        //Se visibiliza el círculo de carga.
        circuloCargaEdCuPelu.setVisibility(View.VISIBLE);

        //Obtiene los datos del peluquero actual desde la BD de Firebase Firestore.
        firestore.collection("peluqueros").document(auth.getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {

                    //Si existen datos...
                    if (task.getResult().exists()) {

                        Peluquero peluquero = task.getResult().toObject(Peluquero.class);

                        //los guarda en variables de clase...
                        emailPeluquero = auth.getCurrentUser().getEmail();
                        nombrePeluquero = peluquero.getNombre();
                        telefonoPeluquero = peluquero.getTelefono();

                        //y los muestra en la activity.
                        tvEdCuPeluMail.setText(auth.getCurrentUser().getEmail());
                        tvEdCuPeluNombre.setText(peluquero.getNombre());
                        tvEdCuPeluTlfn.setText(String.valueOf(peluquero.getTelefono()));
                        tvEdCuPeluLoc.setText(obtieneDireccion(new LatLng(peluquero.getLoc().getLatitude(),
                                peluquero.getLoc().getLongitude())));
                        tvEdCuPeluLocExtra.setText(peluquero.getLocExtra());

                        //Al terminar la carga de datos, invisibiliza el círculo de carga...
                        circuloCargaEdCuPelu.setVisibility(View.INVISIBLE);

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

        bEdCuPeluMail.setEnabled(boleano);
        bEdCuPeluNombre.setEnabled(boleano);
        bEdCuPeluTlfn.setEnabled(boleano);
        bEdCuPeluLoc.setEnabled(boleano);
        bEdCuPeluPw.setEnabled(boleano);
        bEdCuPeluEliCu.setEnabled(boleano);
        botonEdCuPeluAtras.setEnabled(boleano);

    }

    /**
     * Método que se encarga de traducir las coordenadas en una dirección postal.
     *
     * @param coordenadas LatLng con las coordenadas que se quieren traducir.
     *
     * @return Cadena con la dirección postal obtenida mediante las coordenadas.
     */
    private String obtieneDireccion(LatLng coordenadas) {

        try {

            //Obtiene la dirección mediante las coordenadas obtenidas por parámetro.
            List<Address> direcciones = gc.getFromLocation(coordenadas.latitude, coordenadas.longitude, 1);

            //Comprueba que se ha guardado al menos una dirección.
            if(direcciones.size()>0){
                //Si es así, devuelve el primero obtenido.
                return muestraDireccion(direcciones.get(0));
            }else{
                //Si no, devuelve una cadena vacía.
                return "";
            }

        }catch (IOException ioe){
            return "";
        }

    }

    /**
     * Método que se encarga de generar una dirección postal más comprensible.
     *
     * @param direccion Objeto Address con la dirección postal.
     * @return Devuelve una cadena con la dirección postal simplificada.
     */
    protected String muestraDireccion(Address direccion) {

        String numero=direccion.getSubThoroughfare(),
                calle=direccion.getThoroughfare(),
                barrio=direccion.getSubLocality(),
                localidad=direccion.getLocality(),
                provincia=direccion.getSubAdminArea(),
                caOEstado=direccion.getAdminArea(),
                pais=direccion.getCountryName();

        StringBuffer sb=new StringBuffer();

        if(numero!=null){
            sb.append(numero+", ");
        }
        if(calle!=null){
            sb.append(calle+", ");
        }
        if(barrio!=null){
            sb.append(barrio+", ");
        }
        if(localidad!=null){
            sb.append(localidad+", ");
        }
        if(provincia!=null && !provincia.equalsIgnoreCase(localidad)){
            sb.append(provincia+", ");
        }
        if(caOEstado!=null){
            sb.append(caOEstado+", ");
        }
        if(pais!=null){
            sb.append(pais);
        }

        return sb.toString();

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
    public void cambiaEmail(View view){

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
                        if (!email.getText().toString().equalsIgnoreCase(emailPeluquero)) {

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
                                                emailPeluquero = email.getText().toString();
                                                //y lo muestra en la activity.
                                                tvEdCuPeluMail.setText(email.getText().toString());

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
    public void cambiaNombre(View view){

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
                    if(!nombre.getText().toString().equals(nombrePeluquero)){

                        //Actualiza el nombre en la BD del usuario en Firestore.
                        firestore.collection("peluqueros")
                                .document(auth.getCurrentUser().getUid())
                                .update("nombre", nombre.getText().toString())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        cambiaNombresBusqueda(nombre.getText().toString());

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
     * Método que cambia los nombres de búsqueda del peluquero (necesarios para la
     * efectividad del buscador de peluqueros) a partir del nombre modificado
     * por el usuario.
     *
     * @param nombre Cadena con el nombre modificado por el usuario.
     */
    private void cambiaNombresBusqueda(final String nombre){

        /*
        Instancia un objeto de tipo Peluquero y le setea el nombre para,
        posteriormente, obtener el List de nombres de búsqueda, ya que esa
        funcionalidad está embutida dentro de dicho POJO.
        */
        Peluquero peluquero = new Peluquero();
        peluquero.setNombre(nombre);

        //Actualiza los nombre de búsqueda en la BD del usuario en Firestore.
        firestore.collection("peluqueros")
                .document(auth.getCurrentUser().getUid())
                .update("nombresBusqueda", peluquero.getNombresBusqueda())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //Actualiza también el nombre en el perfil del usuario actual.
                        auth.getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().
                                setDisplayName(nombre).build());

                        Toast.makeText(getApplicationContext(),
                                getString(R.string.mensajeEditCuentaNombreExito),
                                Toast.LENGTH_SHORT).show();

                        //Guarda el nuevo nombre en la variable de clase...
                        nombrePeluquero = nombre;
                        //y lo muestra en la activity.
                        tvEdCuPeluNombre.setText(nombre);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.mensajeEditCuentaErrorNombre),
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Método que cambia el teléfono del usuario actual.
     *
     * @param view
     */
    public void cambiaTlfn(View view){

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
                    if(Long.parseLong(tlfn.getText().toString())!=telefonoPeluquero){

                        //Actualiza el teléfono en la BD del usuario en Firestore.
                        firestore.collection("peluqueros")
                                .document(auth.getCurrentUser().getUid())
                                .update("telefono", Long.parseLong(tlfn.getText().toString()))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(getApplicationContext(),
                                                getString(R.string.mensajeEditCuentaTlfnExito),
                                                Toast.LENGTH_SHORT).show();

                                        //Guarda el nuevo teléfono en la variable de clase...
                                        telefonoPeluquero = Long.parseLong(tlfn.getText().toString());
                                        //y lo muestra en la activity.
                                        tvEdCuPeluTlfn.setText(tlfn.getText().toString());

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
     * Método que inicia la activity que ayudará al usuario a cambiar su localización.
     *
     * @param view
     */
    public void cambiaLoc(View view){

        //Inicia la activity de localización, pasándole la ID del peluquero y esperando respuesta.
        startActivityForResult(new Intent(this, RegistroLocActivity.class)
                .putExtra("idPeluquero", auth.getCurrentUser().getUid()), REQUEST_LOC);

    }

    /**
     * Método que cambia la contraseña del usuario actual.
     *
     * @param view
     */
    public void cambiaPw(View view){

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
     * Método que pregunta si se desea eliminar la cuenta del usuario actual.
     *
     * @param view
     */
    public void preguntaEliminarCuenta(View view){

        //Deactiva los botones.
        activaBotones(false);

        //Inicia un AlertDialog que confirmará o no la eliminación de la cuenta.
        new AlertDialog.Builder(this).setMessage(getString(R.string.alDiEditCuentaBorrar))
                .setPositiveButton(getString(R.string.si), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //Visibiliza el círculo de carga.
                        circuloCargaEdCuPelu.setVisibility(View.VISIBLE);

                        //Guarda la ID del usuario actual en una variable.
                        final String idUsuario = auth.getCurrentUser().getUid();

                        //Elimina el usuario actual...
                        auth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){

                                    //y sus datos en Firestore.
                                    firestore.collection("peluqueros").document(idUsuario)
                                            .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                firestore.collection("peluqueros")
                                                        .document(idUsuario)
                                                        .collection("peluqueria")
                                                        .document("tarifas")
                                                        .delete();

                                            }
                                        }
                                    });

                                    Toast.makeText(getApplicationContext(),
                                            getText(R.string.mensajeEditCuentaBorrada)+" "+emailPeluquero+" "+
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

                                    }finally {
                                        circuloCargaEdCuPelu.setVisibility(View.INVISIBLE);
                                        activaBotones(true);
                                    }

                                    circuloCargaEdCuPelu.setVisibility(View.INVISIBLE);
                                    activaBotones(true);

                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.mensajeEditCuentaErrorBorrar),
                                        Toast.LENGTH_SHORT).show();

                                circuloCargaEdCuPelu.setVisibility(View.INVISIBLE);
                                activaBotones(true);
                            }
                        });

                    }
                }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Cierra el dialog y vuelve a activar los botones.
                dialogInterface.dismiss();
                activaBotones(true);
            }
        }).show();

    }

    /**
     * Método que controla el retroceso a la activity anterior.
     *
     * @param view
     */
    public void atras(View view){

        //Se desactivan los botones...
        activaBotones(false);

        //y se cierra la activity.
        finish();

    }
}
