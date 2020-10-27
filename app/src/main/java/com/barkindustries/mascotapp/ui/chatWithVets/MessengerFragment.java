package com.barkindustries.mascotapp.ui.chatWithVets;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.barkindustries.mascotapp.HomeSPActivity;
import com.barkindustries.mascotapp.ui.chatWithVets.Adapters.MessengerAdapter;
import com.barkindustries.mascotapp.ui.chatWithVets.Entities.Firebase.Message;
import com.barkindustries.mascotapp.ui.chatWithVets.Entities.Logic.LMessage;
import com.barkindustries.mascotapp.ui.chatWithVets.Entities.Logic.LUser;
import com.barkindustries.mascotapp.ui.chatWithVets.Persistence.MessengerDAO;
import com.barkindustries.mascotapp.ui.chatWithVets.Persistence.UserDAO;
import com.barkindustries.mascotapp.R;
import com.barkindustries.mascotapp.ui.chatWithVets.Utilities.Constants;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class MessengerFragment extends Fragment {

    // Screen Controls
    private CircleImageView myFriendProfilePic;
    private TextView myFriendName;
    private EditText txtMessage;
    private ImageButton btnSendPic;
    private Button btnSend;

    private RecyclerView rvMessages;
    private MessengerAdapter adapter;

    //Firebase
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;

    //Constant
    private static final int PHOTO_SEND = 1;
    private static final int PHOTO_PERFIL = 2;

    private String profilePicString;
    private String USER_NAME;
    private String myFriendUserId;
    private String friendServiceType;
    private String myServiceType;

    private Boolean friendIsService;
    private Boolean imService;



    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messenger, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            myFriendUserId = bundle.getString("key_receptor");
            friendIsService = bundle.getBoolean("friend_is_service");
            friendServiceType = bundle.getString("friend_service_type");
            myServiceType = bundle.getString("myServiceType");
            imService = bundle.getBoolean("imService");

        }

        else
            getActivity().finish();




        initScreenControls(view);

        setupRecyclerView();

        loadMyFriendNameAndPicture();

        FirebaseDatabase.getInstance().getReference(Constants.MESSAGES_NODE).child(UserDAO.getInstance().getUserKey())
                .child(myFriendUserId).addChildEventListener(new ChildEventListener() {

            //bring user info
            //save user info in a temporal list
            //get info from key
            Map<String, LUser> mapTemporalUsers = new HashMap<>();

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final Message message = dataSnapshot.getValue(Message.class);
                final LMessage lMessage = new LMessage(dataSnapshot.getKey(), message);
                final int position = adapter.addMessage(lMessage);


                if (mapTemporalUsers.get(message.getKeyEmisor()) != null) {
                    lMessage.setlUser(mapTemporalUsers.get(message.getKeyEmisor()));
                    adapter.updateMessage(position, lMessage);
                } else {

                    if (message.getKeyEmisor().equals(mAuth.getUid())) {
                        UserDAO.getInstance().getUserInformationFromKey(message.getKeyEmisor(), new UserDAO.IReturnUser() {
                            @Override
                            public void returnUser(LUser lUser) {
                                mapTemporalUsers.put(message.getKeyEmisor(), lUser);
                                lMessage.setlUser(lUser);
                                adapter.updateMessage(position, lMessage);
                            }

                            @Override
                            public void returnError(String error) {
                                Toast.makeText(MessengerFragment.this.getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                            }
                        }, imService, myServiceType);
                    }
                    else if (message.getKeyEmisor().equals(myFriendUserId)) {
                        UserDAO.getInstance().getUserInformationFromKey(message.getKeyEmisor(), new UserDAO.IReturnUser() {
                            @Override
                            public void returnUser(LUser lUser) {
                                mapTemporalUsers.put(message.getKeyEmisor(), lUser);
                                lMessage.setlUser(lUser);
                                adapter.updateMessage(position, lMessage);
                            }

                            @Override
                            public void returnError(String error) {
                                Toast.makeText(MessengerFragment.this.getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                            }
                        }, friendIsService, friendServiceType);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        verifyStoragePermissions(getActivity());
    }

    private void initScreenControls(@NonNull View view) {

        myFriendProfilePic = view.findViewById(R.id.my_friend_profile_pic);
        myFriendProfilePic.setVisibility(View.GONE);

        myFriendName = view.findViewById(R.id.my_friend_chat_name);
        myFriendName.setVisibility(View.GONE);

        rvMessages = view.findViewById(R.id.rvMessages);
        txtMessage = view.findViewById(R.id.txtMessage);
        btnSend = view.findViewById(R.id.btnSend);
        btnSendPic = view.findViewById(R.id.btnSendPhoto);
        profilePicString = "";

        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();

        btnSend.setOnClickListener(v -> {
            String messageToSend = txtMessage.getText().toString();
            if(!messageToSend.isEmpty()){
                Message message = new Message();
                message.setMessage(messageToSend);
                message.setContainsPhoto(false);
                message.setKeyEmisor(UserDAO.getInstance().getUserKey());
                MessengerDAO.getInstance().newMessage(UserDAO.getInstance().getUserKey(), myFriendUserId, message);




               if(!friendIsService) {
                   FirebaseDatabase.getInstance().getReference(Constants.USERS_NODE + "/" + myFriendUserId + "/" +
                           Constants.NEW_MESSAGES + "/" + mAuth.getCurrentUser().getEmail().replace(".", ","))
                           .setValue(messageToSend);
               }else{
                   //es un service
                   System.out.println(friendServiceType);
                   FirebaseDatabase.getInstance().getReference(Constants.SERVICES_NODE + "/" + friendServiceType + "/" + myFriendUserId + "/" +
                           Constants.NEW_MESSAGES + "/" + mAuth.getCurrentUser().getEmail().replace(".", ","))
                           .setValue(messageToSend);


               }


                txtMessage.setText("");
            }
        });

        btnSendPic.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/jpeg");
            i.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
            startActivityForResult(Intent.createChooser(i,"Select a photo"),PHOTO_SEND);
        });
    }

    private void setupRecyclerView() {
        adapter = new MessengerAdapter(getContext());
        LinearLayoutManager l = new LinearLayoutManager(getContext());
        rvMessages.setLayoutManager(l);
        rvMessages.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                setScrollbar();
            }
        });
    }

    private void setScrollbar(){
        rvMessages.scrollToPosition(adapter.getItemCount()-1);
    }


    //ACA, EN LUGAR DE getReference(USERS), partir de getInstance().addListener....
    //Y adentro navegar el users node/myUid/Friends en busca de myFriendId
    //Y sino, navegar cada servicio hasta encontrar donde estoy yo, ir a Friends/myFriendId

    private void loadMyFriendNameAndPicture() {
        new Thread(() -> {                                ///y si voy a mis amigos directo...
            //hacer un chequeo de si es un PO o un SP


            FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(Constants.USERS_NODE).hasChild(myFriendUserId)){
                        myFriendName.setText(dataSnapshot.child(Constants.USERS_NODE).child(myFriendUserId).child("UsernameText").getValue().toString());
                        myFriendName.setVisibility(View.VISIBLE);
                        Glide.with(MessengerFragment.this).load(dataSnapshot.child(Constants.USERS_NODE).child(myFriendUserId).child("profilePicURL").getValue()).into(myFriendProfilePic);
                        myFriendProfilePic.setVisibility(View.VISIBLE);
                    }else{
                        //si no esta en los usuarios, y ya lo agregue, debe estar en los servicios
                        for(DataSnapshot ds : dataSnapshot.child(Constants.SERVICES_NODE).getChildren()){
                            for(DataSnapshot sp : ds.getChildren()){
                                if(Objects.requireNonNull(sp.getKey()).equals(myFriendUserId)){
                                    myFriendName.setText(sp.child("UsernameText").getValue().toString());
                                    myFriendName.setVisibility(View.VISIBLE);
                                    Glide.with(MessengerFragment.this).load(sp.child("profilePicURL").getValue()).into(myFriendProfilePic);
                                    myFriendProfilePic.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });




            /*if(!isService) {
                FirebaseDatabase.getInstance().getReference(Constants.USERS_NODE + "/" + myFriendUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        myFriendName.setText(dataSnapshot.child("UsernameText").getValue().toString());
                        Glide.with(MessengerFragment.this).load(dataSnapshot.child("profilePicURL").getValue()).into(myFriendProfilePic);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }else //es un service
            {
                FirebaseDatabase.getInstance().getReference(Constants.SERVICES_NODE + "/" + serviceType + "/" + myFriendUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        myFriendName.setText(dataSnapshot.child("UsernameText").getValue().toString());
                        Glide.with(MessengerFragment.this).load(dataSnapshot.child("profilePicURL").getValue()).into(myFriendProfilePic);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

            }*/
        }).start();
    }

    private static void verifyStoragePermissions(Activity activity) {
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int REQUEST_EXTERNAL_STORAGE = 1;
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PHOTO_SEND && resultCode == RESULT_OK){
            Uri u = data.getData();
            storageReference = storage.getReference("chat_images");//imagenes_chat
            final StorageReference fotoReferencia = storageReference.child(u.getLastPathSegment());
            fotoReferencia.putFile(u).continueWithTask(task -> {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return fotoReferencia.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Uri uri = task.getResult();
                    Message message = new Message();
                    message.setMessage("Image");
                    message.setUrlPic(uri.toString());
                    message.setContainsPhoto(true);
                    message.setKeyEmisor(UserDAO.getInstance().getUserKey());
                    MessengerDAO.getInstance().newMessage(UserDAO.getInstance().getUserKey(), myFriendUserId,message);
                }
            });
        }
    }
}
