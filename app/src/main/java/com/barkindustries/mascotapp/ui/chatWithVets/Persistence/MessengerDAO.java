package com.barkindustries.mascotapp.ui.chatWithVets.Persistence;

import com.barkindustries.mascotapp.ui.chatWithVets.Entities.Firebase.Message;
import com.barkindustries.mascotapp.ui.chatWithVets.Utilities.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MessengerDAO {

    private static MessengerDAO messengerDAO;

    private FirebaseDatabase database;
    private DatabaseReference messengerReference;

    public static MessengerDAO getInstance(){
        if(messengerDAO == null)
            messengerDAO = new MessengerDAO();
        return messengerDAO;
    }

    private MessengerDAO(){
        database = FirebaseDatabase.getInstance();
        messengerReference = database.getReference(Constants.MESSAGES_NODE);
        //storage = FirebaseStorage.getInstance();
        //referenceUsuarios = database.getReference(Constantes.NODO_USUARIOS);
        //referenceFotoDePerfil = storage.getReference("Fotos/FotoPerfil/"+getKeyUsuario());
    }

    public void newMessage(String keyEmisor, String keyReceptor, Message message){
        DatabaseReference referenceEmisor = messengerReference.child(keyEmisor).child(keyReceptor);
        DatabaseReference referenceReceptor = messengerReference.child(keyReceptor).child(keyEmisor);
        referenceEmisor.push().setValue(message);
        referenceReceptor.push().setValue(message);
    }

}
