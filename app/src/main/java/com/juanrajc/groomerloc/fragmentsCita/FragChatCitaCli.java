package com.juanrajc.groomerloc.fragmentsCita;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.juanrajc.groomerloc.CitaClienteActivity;
import com.juanrajc.groomerloc.R;
import com.juanrajc.groomerloc.adaptadores.AdaptadorMensajes;
import com.juanrajc.groomerloc.clasesBD.Mensaje;

import java.util.ArrayList;
import java.util.Calendar;

public class FragChatCitaCli extends Fragment {

    private String idClita;

    //Objetos de los elementos de la vista.
    private FloatingActionButton fabMensajeCli;
    private EditText inputChatCli;
    private ListView listMensajesCli;

    //Objeto de la lista de mensajes.
    private ArrayList<Mensaje> listaMensajes;

    //Objetos de Firebase (Autenticación y BD Firestore).
    private FirebaseAuth usuario;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_chat_cita_cli, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Instancia de los elementos de la vista.
        fabMensajeCli = getActivity().findViewById(R.id.fabMensajeCli);
        inputChatCli = getActivity().findViewById(R.id.inputChatCli);
        listMensajesCli = getActivity().findViewById(R.id.listMensajesCli);

        //Instancias de la autenticación y la base de datos de Firebase.
        usuario = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        //Recoge la ID de la cita desde la activity que carga este fragment.
        idClita = ((CitaClienteActivity)getContext()).getIdCita();

        //Listener del FAB que se utiliza para regoger el texto introducido.
        fabMensajeCli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Comprueba que se ha introducido algo en el campo del mensaje.
                if(inputChatCli.getText().length()>0) {

                    //Añade el mensaje al chat de la cita recibida.
                    firestore.collection("citas").document(idClita)
                            .collection("chat").add(new Mensaje(usuario.getCurrentUser()
                            .getDisplayName(), inputChatCli.getText().toString(), Calendar.getInstance()
                            .getTime())).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {

                                //Si se guarda correctamente, borra el campo de entrada de texto.
                                inputChatCli.setText("");

                            } else {
                                Toast.makeText(getActivity(), getActivity().getString(R.string.mensajeNoEnviado),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.mensajeNoEnviado),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }
        });

        muestraMensajes();

        listenerMensajes();

    }

    /**
     * Método que muestra los mensajes guardados en la cita recibida.
     */
    private void muestraMensajes(){

        //Obtiene los mensajes del chat de la cita, ordenados por fecha.
        firestore.collection("citas").document(idClita).collection("chat")
                .orderBy("fecha").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    //Crea una lista de mensajes...
                    listaMensajes = new ArrayList<Mensaje>();

                    //y los guarda en el uno a uno.
                    for(QueryDocumentSnapshot doc:task.getResult()){
                        listaMensajes.add(doc.toObject(Mensaje.class));
                    }

                    //Finalmente setea el adaptador con los mensajes obtenidos.
                    listMensajesCli.setAdapter( new AdaptadorMensajes(getActivity(), listaMensajes));

                }else{
                    Toast.makeText(getActivity(), getActivity().getString(R.string.mensajesNoCargados),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), getActivity().getString(R.string.mensajesNoCargados),
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Método que crea el lístener con el que el chat de la cita se actualizará en tiempo real.
     */
    private void listenerMensajes(){

        /*
        Crea el listener que se ejecutará cada vez que haya una modificación en la colección del
        chat de la cita.
        */
        firestore.collection("citas").document(idClita).collection("chat")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots,
                                        @javax.annotation.Nullable FirebaseFirestoreException e) {

                        //Comprueba si el QDS está vacío.
                        if(queryDocumentSnapshots.isEmpty()){

                        }else{

                            //Obtiene los cambios producidos.
                            for(DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()){

                                switch (doc.getType()){

                                    //Si se añadió un elemento.
                                    case ADDED:

                                        if(getActivity()!=null) {
                                            muestraMensajes();
                                        }

                                        break;

                                    //Si se modificó un elemento.
                                    case MODIFIED:

                                        if(getActivity()!=null) {
                                            muestraMensajes();
                                        }

                                        break;

                                    //Si se eliminó un elemento.
                                    case REMOVED:

                                        if(getActivity()!=null) {
                                            muestraMensajes();
                                        }

                                }

                            }

                        }

                    }
                });

    }

}
