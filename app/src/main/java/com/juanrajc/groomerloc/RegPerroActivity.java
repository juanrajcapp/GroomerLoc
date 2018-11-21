package com.juanrajc.groomerloc;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegPerroActivity extends AppCompatActivity {

    //Objetos de la vista de la activity.
    private RadioGroup grupoSexo;
    private RadioButton perroMacho, perroHembra;
    private EditText nombrePerro, razaPerro, pesoPerro, comentPerro;
    private ImageView ivPerro;

    //Objeto del botón registro de perro de la vista.
    private Button botonRegPerro;

    //Objeto del usuario actual.
    private FirebaseUser usuario;
    private FirebaseFirestore firestore;

    private String rutaFoto;

    private static final int REQUEST_CAMARA=1, REQUEST_GALERIA=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_perro);

        //Instancia del usuario actual y de la base de datos Firestore.
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        //Instancia del grupo de botones de radio.
        grupoSexo = (RadioGroup) findViewById(R.id.rgRegSexo);

        //Instancia de los botones de radio para seleccionar el sexo de la mascota.
        perroMacho = (RadioButton) findViewById(R.id.rbRegMacho);
        perroHembra = (RadioButton) findViewById(R.id.rbRegHembra);

        //Instancia de los campos de registro de la vista.
        nombrePerro = (EditText) findViewById(R.id.etRegNombrePerro);
        razaPerro = (EditText) findViewById(R.id.etRegRaza);
        pesoPerro = (EditText) findViewById(R.id.etRegPeso);
        comentPerro = (EditText) findViewById(R.id.etRegComent);

        ivPerro = (ImageView) findViewById(R.id.ivPerro);

        //Instancia del botón de registro del perro de la vista.
        botonRegPerro = (Button) findViewById(R.id.botonRegPerro);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){

            case REQUEST_CAMARA:

                if(resultCode==RESULT_OK){
                    ivPerro.setImageBitmap(BitmapFactory.decodeFile(rutaFoto));
                }

                break;

            case REQUEST_GALERIA:

                //Controla que si no se hace o se selecciona una imagen, al volver no produzca una excepción de valor nulo.
                if(data!=null && data.getExtras()!=null && resultCode ==RESULT_OK){

                    Bundle extras = data.getExtras();
                    Bitmap bm = (Bitmap) extras.get("data");
                    ivPerro.setImageBitmap(bm);

                }

                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Después de una patición de permisos, se recarga el dialog.
        dialogoImagen(null);

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        rutaFoto = image.getAbsolutePath();
        return image;


    }

    /**
     * Método que muestra el diálogo que permite seleccionar el método de obtención de imagen.
     *
     * @param view
     */
    protected void dialogoImagen(View view){

        //Array con las opciones mostradas en el dialog.
        final CharSequence[] opciones = {getString(R.string.opCamara), getString(R.string.opGaleria), getString(R.string.opCancelar)};

        //Instancia del dialog con el estilo definido en "styles".
        AlertDialog.Builder ad = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);

        //Crea un TV para personalizar el título del dialog.
        TextView tituloDialog = new TextView(this);
        tituloDialog.setText(getString(R.string.tituloDialog));
        tituloDialog.setGravity(Gravity.CENTER);
        tituloDialog.setBackgroundColor(Color.GRAY);
        tituloDialog.setTextColor(Color.WHITE);
        tituloDialog.setPadding(8, 8, 8, 8);
        tituloDialog.setTextSize(20);

        //Añade el TV al dialog.
        ad.setCustomTitle(tituloDialog);

        //Listener que controla cual es la opción seleccionada en el dialog.
        ad.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (opciones[i].equals(getString(R.string.opCamara))) {
                    opcionCamara();
                } else if (opciones[i].equals(getString(R.string.opGaleria))) {
                    opcionGaleria();
                } else if (opciones[i].equals(getString(R.string.opCancelar))) {
                    dialogInterface.dismiss();
                }

            }
        });

        //Muestra el dialog.
        ad.show();

    }

    /**
     *
     */
    private void opcionCamara(){

        if(permisosCamara()) {

            Intent intentCamara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if(intentCamara.resolveActivity(getPackageManager())!=null){

                File archivoFoto =null;

                try{
                    archivoFoto=createImageFile();
                }catch (IOException ioe){

                }

                if(archivoFoto!=null){

                    Uri URIFoto = FileProvider.getUriForFile(this, "com.example.android.fileprovider", archivoFoto);

                    intentCamara.putExtra(MediaStore.EXTRA_OUTPUT, URIFoto);

                    startActivityForResult(intentCamara, REQUEST_CAMARA);

                }

            }

        }

    }

    /**
     *
     */
    private void opcionGaleria(){

        if(permisosLecturaArchivos()) {

            Intent intentGaleria = new Intent(MediaStore.INTENT_ACTION_MEDIA_SEARCH);
            startActivityForResult(intentGaleria, REQUEST_GALERIA);

        }

    }

    /**
     * Método que comprueba y pide los permisos para usar la cámara del dispositivo.
     *
     * @return Devuelve un booleano verdadero si los permisos están aceptados, o falso si no los están.
     */
    private boolean permisosCamara(){

        //Comprueba si tenemos permisos para acceder a la cámara.
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            // Solicita permiso en caso de que no disponga.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            return false;
        }

        return true;

    }

    /**
     * Método que comprueba y pide los permisos para leer archivos del almacenamiento externo.
     *
     * @return Devuelve un booleano verdadero si los permisos están aceptados, o falso si no los están
     */
    private boolean permisosLecturaArchivos(){

        //Comprueba si tenemos permisos para acceder a la galería.
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            // Solicita permiso en caso de que no disponga.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            return false;
        }

        return true;

    }

    /**
     * Método que controla la validez de los campos del formulario de la activity de registro de mascotas.
     *
     * @return Devuelve true si los campos son válidos, o false si no lo son.
     */
    private boolean compruebaCampos(){

        //Comprueba que los campos están correctamente completados.
        if(nombrePerro.getText().toString().length()>0 && razaPerro.getText().toString().length()>0
                && pesoPerro.getText().toString().length()>0){

            //Comprueba que se ha seleccionado uno de los dos sexos.
            if(perroMacho.isChecked() || perroHembra.isChecked()){

                //Devuelve verdadero cuando el formulario está correcto.
                return true;

            } else {
                Toast.makeText(this, getString(R.string.mensajeSexo), Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, getString(R.string.mensajeCamposPerro), Toast.LENGTH_SHORT).show();
        }

        return false;

    }

    /**
     * Método que se ejecuta cuando se pulsa el botón de registro del perro.
     *
     * @param view
     */
    protected  void regPerro (View view){

        if(compruebaCampos()){

            botonRegPerro.setEnabled(false);

        }

    }

    /**
     * Método que controla el retroceso a la activity anterior.
     *
     * @param view
     */
    protected void atras (View view){

        finish();

    }

}
