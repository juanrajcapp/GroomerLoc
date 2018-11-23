package com.juanrajc.groomerloc;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.juanrajc.groomerloc.clasesBD.Perro;

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

    //Objeto del usuario actual, de la BD Firestore y de la referencia al almacenamiento de ficheros Storage.
    private FirebaseUser usuario;
    private FirebaseFirestore firestore;
    private StorageReference referenciaFoto;

    //Uri de la foto temporal o almacenada en el dispositivo.
    private Uri rutaFoto;

    //Constantes con los posibles resultados devueltos a la activity actual.
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

        //Instancia de la imagen mostrada en la activity.
        ivPerro = (ImageView) findViewById(R.id.ivPerro);

        //Instancia del botón de registro del perro de la vista.
        botonRegPerro = (Button) findViewById(R.id.botonRegPerro);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Si el resultado devuelto a la activity es satisfactoria...
        if(resultCode==RESULT_OK) {

            //Comprueba mediante un switch el tipo de resultado devuelto.
            switch (requestCode) {

                case REQUEST_CAMARA:

                    //Muestra la imagen mediante una Uri.
                    ivPerro.setImageURI(rutaFoto);

                    break;

                case REQUEST_GALERIA:

                    //Guarda la Uri devuelta en la variable de la clase.
                    rutaFoto=data.getData();
                    //Muestra la imagen mediante una Uri.
                    ivPerro.setImageURI(rutaFoto);

            }
        } else{
            Toast.makeText(this, getString(R.string.mensajeRequestFoto), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Después de una patición de permisos, se recarga el dialog.
        dialogoImagen(null);

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
                    //Cierra el dialog con las opciones.
                    dialogInterface.dismiss();
                }

            }
        });

        //Muestra el dialog.
        ad.show();

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
     * Método que crea un File temporal con la imagen tomada con la cámara del dispositivo.
     *
     * @return File con la imagen temporal.
     */
    private File createImageFile(){
        // Crea un nombre para el archivo con la imagen.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {

            File image = File.createTempFile(
                    imageFileName,  /* prefijo */
                    ".jpg",         /* sufijo */
                    storageDir      /* directorio */
            );

            //Guarda la ruta absoluta para luego acceder al recurso.
            rutaFoto = Uri.parse("file:"+image.getAbsolutePath());
            return image;

        }catch (IOException ioe){
            return null;
        }

    }

    /**
     * Método que inicia la cámara.
     */
    private void opcionCamara(){

        //Si el permiso de acceso a la cámara está aceptado...
        if(permisosCamara()) {

            //crea el intent que inicia la cámara del dispositivo.
            Intent intentCamara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            //Comprueba que existe una activity que reciba el intent (aplicación de la cámara).
            if(intentCamara.resolveActivity(getPackageManager())!=null){

                //Crea un archivo temporal, el cual alojará la imagen tomada por la cámara.
                File archivoFoto=createImageFile();

                //Comprueba que se ha creado correctamente.
                if(archivoFoto!=null){

                    //Se añade un extra al intent con el File que contendrá la imagen.
                    intentCamara.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this,
                            "com.example.android.fileprovider", archivoFoto));

                    //Inicia la activity con el intent.
                    startActivityForResult(intentCamara, REQUEST_CAMARA);

                }
            }
        }
    }

    /**
     * Método que abre el gestor de archivos.
     */
    private void opcionGaleria(){

        //Si el permiso de lectura de archivos está aceptado...
        if(permisosLecturaArchivos()) {

            //crea el intent que inicia el gestor de archivos.
            Intent intentGaleria = new Intent(Intent.ACTION_GET_CONTENT);
            //Indica el tipo de archivo que se va a mostrar.
            intentGaleria.setType("image/*");

            //Inicia la activity con el intent.
            startActivityForResult(intentGaleria, REQUEST_GALERIA);

        }

    }

    /**
     * Método que traduce la selección de sexo a cadena.
     *
     * @return String con el sexo seleccionado.
     */
    private String obtieneSexo(){

        if(perroMacho.isChecked()){

            return "XY";

        } else if(perroHembra.isChecked()){

            return "XX";

        } else{
            return getString(R.string.mensajeNoEspecificado);
        }

    }

    /**
     * Método que comprueba si el perro que se intenta registrar lo está ya o no.
     *
     * @param nombre String con el nombre del perro.
     */
    private void compruebaExistenciaPerro(String nombre){

        //Referencia al registro (documento) de la base de datos en la que se encontrarían los datos del perro.
        DocumentReference dr=firestore.collection("clientes").document(usuario.getUid())
                .collection("perros").document(nombre);

        //Crea un listener que recupera el contenido del documento especificado anteriormente.
        dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {

                    DocumentSnapshot doc = task.getResult();

                    //Si existe el registro...
                    if (doc.exists()) {
                        preguntaSobreescrituraPerro();
                    //si no...
                    }else {
                        creaPerro();
                    }

                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.mensajeFalloBD), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Método que se encarga de preguntar si se desea sobreescribir la información existente del perro.
     */
    private void preguntaSobreescrituraPerro(){

        DialogInterface.OnClickListener dialogo = new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                switch (i){

                    //Si se acepta sobreescribir...
                    case DialogInterface.BUTTON_POSITIVE:
                        creaPerro();
                        break;

                    //si no...
                    case DialogInterface.BUTTON_NEGATIVE:
                        //Borra el campo del nombre del perro...
                        nombrePerro.setText("");
                        //y vuelve a activar el botón de registro de perro.
                        botonRegPerro.setEnabled(true);
                }

            }

        };

        //Crea y muestra el diálogo de alerta.
        new AlertDialog.Builder(this).setMessage(nombrePerro.getText().toString()+" "+getString(R.string.mensajeSobreescrituraPerro))
                .setPositiveButton(getString(R.string.si), dialogo).setNegativeButton(getString(R.string.no), dialogo).show();

    }

    /**
     * Método que se encarga de crear el registro del perro con los datos introducidos en la activity.
     */
    private void creaPerro(){

        firestore.collection("clientes")
                .document(usuario.getUid()).collection("perros")
                .document(nombrePerro.getText().toString()).set(new Perro(razaPerro.getText().toString(),
                obtieneSexo(), comentPerro.getText().toString(), Float.parseFloat(pesoPerro.getText().toString())));

        //Si se ha especificado la ruta de una foto...
        if(rutaFoto!=null) {
            guardaFoto();
        }

        //Finaliza la activity.
        finish();

    }

    /**
     * Método que se encarga de guardar la foto en Firebase Storage.
     */
    private void guardaFoto(){

        //Crea la referencia de Firebase Storage donde se va a guardar la foto.
        referenciaFoto = FirebaseStorage.getInstance().getReference("fotos/" + usuario.getUid()
                + "/" + nombrePerro.getText().toString() + ".jpg");

        //Sube la foto a la referencia previamente creada. Se guarda el return de la acción para controlar la subida.
        UploadTask controlSubida = referenciaFoto.putFile(rutaFoto);

        //Si la subida falla...
        controlSubida.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.mensajeSubidaFallida), Toast.LENGTH_SHORT).show();
            }
        //Si la subida resulta exitosa...
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(), getString(R.string.mensajeSubidaCorrecta), Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Método que se ejecuta cuando se pulsa el botón de registro del perro.
     *
     * @param view
     */
    protected  void regPerro (View view){

        if(compruebaCampos()){

            //Desactiva el botón de registro de perro.
            botonRegPerro.setEnabled(false);

            compruebaExistenciaPerro(nombrePerro.getText().toString());

        }

    }

    /**
     * Método que controla el retroceso a la activity anterior.
     *
     * @param view
     */
    protected void atras (View view){

        //Finaliza la activity.
        finish();

    }

}
