package com.barkindustries.mascotapp.ui.chatWithVets.Adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.barkindustries.mascotapp.ui.chatWithVets.Entities.Firebase.User;
import com.barkindustries.mascotapp.ui.chatWithVets.Entities.Logic.LUser;
import com.barkindustries.mascotapp.ui.chatWithVets.Holder.UserViewHolder;
import com.barkindustries.mascotapp.R;
import com.barkindustries.mascotapp.ui.chatWithVets.Utilities.Constants;
import com.barkindustries.mascotapp.ui.chatWithVets.SeeUsersFragment;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatsListAdapter extends FirebaseRecyclerAdapter<User, UserViewHolder> {

    private String userId;
    private SeeUsersFragment parentFragment;
    private FirebaseDatabase database;
    private Boolean imService;
    private String serviceType;

    public ChatsListAdapter(@NonNull FirebaseRecyclerOptions<User> options, String userId, SeeUsersFragment parentFragment,
                            FirebaseDatabase database, Boolean imService, String serviceType) {
        super(options);
        this.userId = userId;
        this.parentFragment = parentFragment;
        this.database = database;
        this.imService = imService;
        this.serviceType = serviceType; //podria ser que sea null, pero si es null, entonces no deberia entrar en ningun isService...
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull final UserViewHolder holder, int position, @NonNull final User model) {
        Glide.with(parentFragment).load(model.getProfilePicURL()).into(holder.getCivProfilePic());
        holder.getTxtUserName().setText(model.getUsernameText());

        if(model.isTinderOrigin()){
            holder.getOriginSymbol().setVisibility(View.VISIBLE);
        }

        DatabaseReference ref = database.getReference();

        ref.addValueEventListener(new ValueEventListener() {
            //Al ser addValueEventListener, como que no es una vez, sino que lo hace all the time
            //De esta forma, al mandar un mensaje nuevo, aparece la estrellita instantaneamente
            //Its kind of repeated, but well, lets see first if it works
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!imService) {
                    if (snapshot.child(Constants.USERS_NODE).child(userId).child(Constants.NEW_MESSAGES).getValue() != null)
                        if (snapshot.child(Constants.USERS_NODE).child(userId).child(Constants.NEW_MESSAGES).child(model.getEmail().replace(".", ",")).getValue() != null) {
                            // The child does exist
                            holder.getCircle().setVisibility(View.VISIBLE);
                        } else {
                            holder.getCircle().setVisibility(View.GONE);
                        }

                }else //it is a service
                {
                    if (snapshot.child(Constants.SERVICES_NODE).child(serviceType).child(userId).child(Constants.NEW_MESSAGES).getValue() != null)
                        if (snapshot.child(Constants.SERVICES_NODE).child(serviceType).child(userId).child(Constants.NEW_MESSAGES).child(model.getEmail().replace(".", ",")).getValue() != null) {
                            // The child does exist
                            holder.getCircle().setVisibility(View.VISIBLE);
                        } else {
                            holder.getCircle().setVisibility(View.GONE);
                        }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        final LUser lUser = new LUser(getSnapshots().getSnapshot(position).getKey(), model);


        holder.getPrincipalLayout().setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("key_receptor", lUser.getKey());
            bundle.putBoolean("friend_is_service", lUser.getUser().getService());
            bundle.putString("friend_service_type", lUser.getUser().getServiceType());
            bundle.putBoolean("imService", imService);
            bundle.putString("myServiceType", serviceType);
            if(!imService)
                 database.getReference(Constants.USERS_NODE + "/" + userId + "/"+ Constants.NEW_MESSAGES + "/" + model.getEmail().replace(".", ",")).removeValue();
            else
                database.getReference(Constants.SERVICES_NODE + "/" + serviceType + "/" + userId + "/"+ Constants.NEW_MESSAGES + "/" + model.getEmail().replace(".", ",")).removeValue();

            final NavController navController =  Navigation.findNavController(v);
            navController.navigate(R.id.messengerFragment, bundle);
        });
    }
}
