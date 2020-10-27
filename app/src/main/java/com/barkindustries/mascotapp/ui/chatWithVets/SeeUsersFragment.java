package com.barkindustries.mascotapp.ui.chatWithVets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.barkindustries.mascotapp.HomeSPActivity;
import com.barkindustries.mascotapp.ui.chatWithVets.Adapters.*;
import com.barkindustries.mascotapp.ui.chatWithVets.Entities.Firebase.ServiceProvider;
import com.barkindustries.mascotapp.ui.chatWithVets.Entities.Firebase.User;
import com.barkindustries.mascotapp.R;
import com.barkindustries.mascotapp.ui.chatWithVets.Utilities.Constants;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SeeUsersFragment extends Fragment {
    //view
    private RecyclerView rvUsers;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseRecyclerAdapter adapter;
    private FirebaseDatabase database;
    private DatabaseReference usersReference;
    private String userId;

    private Boolean imService;
    private String myService;

    private EditText txtNewEmail;

    private HomeSPActivity myActivity;


    public SeeUsersFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_see_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getUid();
        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference(Constants.USERS_NODE);
        myService = null;
        imService = false;

        /*Si quisieramos que un SP no muestre ni la opcion de poder agregar via mail
        if(getActivity().getClass() == HomeSPActivity.class){
            requireView().findViewById(R.id.btnAdd).setVisibility(View.GONE);
            requireView().findViewById(R.id.txtAddUser).setVisibility(View.GONE);
        }*/




        // Screen Controls
        Button addUserBtn = view.findViewById(R.id.btnAdd);
        addUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                becomeFriendWithUser(txtNewEmail.getText().toString(), userId, database, false, imService);
            }
        });

        txtNewEmail = view.findViewById(R.id.txtAddUser);

    if(getActivity().getClass() == HomeSPActivity.class){
        imService = true;
        myActivity = (HomeSPActivity) getActivity();
        myService = myActivity.getServiceType();
    }





        setupRecyclerView(view); ////////////////////////////////////
        
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvUsers);


    }

    private void setupRecyclerView(@NonNull View view) {
        rvUsers = view.findViewById(R.id.rvUsers);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        rvUsers.setLayoutManager(linearLayoutManager);

        Query query;

        if(!imService) {
            System.out.println("IM NOT A SERVICE");
             query = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(Constants.USERS_NODE).child(userId).child(Constants.FRIENDS);
        }else {
            System.out.println("I AM A SERVICE");
            System.out.println(myService);
            query = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(Constants.SERVICES_NODE).child(myService).child(userId).child(Constants.FRIENDS);
        }

        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(query, User.class)
                        .build();

        //Una solucion para chat de servicio seria mandarle el isService por aca.. nose si es lo mejor pero bueno
        adapter = new ChatsListAdapter(options, userId, this, database, imService, myService);
        rvUsers.setAdapter(adapter);
    }

    public void becomeFriendWithUser(String newEmail, String userId, FirebaseDatabase database, boolean fromTinder, boolean imService) {
        final String wantedEmail = newEmail; //esto quedo de una version anterior :(

        if(!wantedEmail.isEmpty()){
            database.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String myEmail, myName, myPic;
                    Object myRealNameObject, myPhoneObject;

                    if(!imService) {
                         myEmail = dataSnapshot.child(Constants.USERS_NODE).child(userId).child(Constants.EMAIL).getValue().toString();
                         myName = dataSnapshot.child(Constants.USERS_NODE).child(userId).child(Constants.NAME).getValue().toString();
                        myPic = dataSnapshot.child(Constants.USERS_NODE).child(userId).child(Constants.PROFILE_PIC_URL).getValue().toString();
                         myRealNameObject = dataSnapshot.child(Constants.USERS_NODE).child(userId).child("RealName").getValue();
                         myPhoneObject = dataSnapshot.child(Constants.USERS_NODE).child(userId).child("Phone").getValue();
                    }else{
                        myEmail = dataSnapshot.child(Constants.SERVICES_NODE).child(myService).child(userId).child(Constants.EMAIL).getValue().toString();
                        myName = dataSnapshot.child(Constants.SERVICES_NODE).child(myService).child(userId).child(Constants.NAME).getValue().toString();
                        myPic = dataSnapshot.child(Constants.SERVICES_NODE).child(myService).child(userId).child(Constants.PROFILE_PIC_URL).getValue().toString();
                        myRealNameObject = dataSnapshot.child(Constants.SERVICES_NODE).child(myService).child(userId).child("RealName").getValue();
                        myPhoneObject = dataSnapshot.child(Constants.SERVICES_NODE).child(myService).child(userId).child("Phone").getValue();


                    }



                    String myRealName = null;
                    String myPhone = null;

                    if(myRealNameObject != null)
                        myRealName = myRealNameObject.toString();
                    if(myPhoneObject != null)
                        myPhone =  myPhoneObject.toString();
                    

                    User u;
                    User me = new User(myPic, myName, myEmail, myRealName, myPhone);
                    me.setTinderOrigin(fromTinder);


                    String wantedEmailAUX = wantedEmail;

                    //We search on Users Node
                    for(DataSnapshot ds  : dataSnapshot.child(Constants.USERS_NODE).getChildren()){
                        u = ds.getValue(User.class);
                        if(u != null) {
                            u.setTinderOrigin(fromTinder);

                            if(wantedEmail.contains("Email: ")){
                                wantedEmailAUX = wantedEmail.substring(6).toString();
                                u.setEmail(wantedEmailAUX);
                            }
                            //Adding new user
                            if (u.getEmail().equals(wantedEmailAUX)) {
                                u.setService(false);
                                database.getReference(Constants.USERS_NODE + "/" + userId + "/" + Constants.FRIENDS + "/" + ds.getKey()).setValue(u);
                                database.getReference(Constants.USERS_NODE + "/" + ds.getKey() + "/" + Constants.FRIENDS + "/" + userId).setValue(me);

                                return;
                            }
                        }
                    }
                    //We search on Services Nodes
                    //No deberiamos hacer lo de wantedEmailAUX aca, pues no se llama desde lost pets,
                    //pero tenerlo en cuenta si se quisiera hacer
                    ServiceProvider serviceP;
                    for(DataSnapshot ds  : dataSnapshot.child(Constants.SERVICES_NODE).getChildren()){
                        for(DataSnapshot sp : ds.getChildren()){

                            serviceP = sp.getValue(ServiceProvider.class);
                            if(serviceP != null) {
                                serviceP.setTinderOrigin(fromTinder);

                                if (serviceP.getEmail().equals(wantedEmail)) {
                                    serviceP.setService(true);
                                    serviceP.setServiceType(ds.getKey());
                                    database.getReference(Constants.USERS_NODE + "/" + userId + "/" + Constants.FRIENDS + "/" + sp.getKey()).setValue(serviceP);
                                    database.getReference(Constants.SERVICES_NODE + "/" + ds.getKey() + "/" + sp.getKey() + "/" + Constants.FRIENDS + "/" + userId).setValue(me);

                                    return;

                                }
                            }
                        }
                    }
                    Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
        else
            Toast.makeText(requireContext(), "Invalid Email", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            deleteUser(position);
            adapter.notifyDataSetChanged();
        }
    };

    private void deleteUser(final int id){

        database.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String uidFriendToDelete;
                int i = 0;
                for(DataSnapshot ds : dataSnapshot.child(Constants.USERS_NODE).child(userId).child(Constants.FRIENDS).getChildren()){
                    if(i == id){
                        uidFriendToDelete = ds.getKey();
                        //delete user
                        database.getReference(Constants.USERS_NODE +"/" + mAuth.getUid() + "/" + Constants.FRIENDS + "/" + uidFriendToDelete).removeValue();

                        //aca eliminamos NUESTRO chat con dicho usuario, PERO no eliminamos SU chat con nosotros, eso que lo haga el si quiere

                        //if my friend delted me as well, everything must be deleted
                        if(!dataSnapshot.child(Constants.USERS_NODE).child(uidFriendToDelete).child(Constants.FRIENDS).hasChild(mAuth.getUid())){
                            //delete all info between me and the target user
                            database.getReference(Constants.MESSAGES_NODE + "/" + uidFriendToDelete + "/" + mAuth.getUid()).removeValue();

                            //delete the last message from that user
                            database.getReference(Constants.USERS_NODE + "/" + uidFriendToDelete + "/"+ Constants.NEW_MESSAGES +"/" + mAuth.getCurrentUser().getEmail().replace(".", ",")).removeValue();

                        }

                        //his info is deleted from my "part"
                        database.getReference(Constants.MESSAGES_NODE + "/" + mAuth.getUid() + "/" + uidFriendToDelete).removeValue();
                        return;
                    }
                    i++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
