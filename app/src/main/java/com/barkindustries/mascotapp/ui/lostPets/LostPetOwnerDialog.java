package com.barkindustries.mascotapp.ui.lostPets;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.barkindustries.mascotapp.R;
import com.barkindustries.mascotapp.ui.chatWithVets.SeeUsersFragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LostPetOwnerDialog extends Dialog implements android.view.View.OnClickListener{

    private String ownerId, petName;
    private LostPetDialog dialog;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    private ImageView image;
    private TextView emailTextView, nameTextView, titleTextView, usernameTextView, phoneTextView, reportedByYou, alreadyFriendsTextView;
    private Button closeButton, addContact, goBack;
    private ProgressBar progressBar;

    public LostPetOwnerDialog(@NonNull Context context, LostPetDialog dialog, String ownerId, String petName) {
        super(context);
        this.ownerId = ownerId;
        this.dialog = dialog;
        this.petName = petName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.lost_pet_owner_dialog);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();


        image = findViewById(R.id.lost_pet_owner_imageView);
        titleTextView = findViewById(R.id.lost_pet_owner_name_textView);
        nameTextView = findViewById(R.id.owner_name);
        emailTextView = findViewById(R.id.owner_email);
        phoneTextView = findViewById(R.id.owner_phone);
        usernameTextView = findViewById(R.id.owner_username);
        closeButton = findViewById(R.id.close_button);
        addContact = findViewById(R.id.add_contact_button);
        progressBar = findViewById(R.id.progressBar);
        goBack = findViewById(R.id.go_back_arrow);
        reportedByYou = findViewById(R.id.reported_by_you);
        alreadyFriendsTextView = findViewById(R.id.already_friends_textView);

        image.setVisibility(View.INVISIBLE);
        usernameTextView.setVisibility(View.INVISIBLE);
        phoneTextView.setVisibility(View.GONE);
        addContact.setVisibility(View.INVISIBLE);
        titleTextView.setVisibility(View.INVISIBLE);
        nameTextView.setVisibility(View.GONE);
        emailTextView.setVisibility(View.INVISIBLE);
        reportedByYou.setVisibility(View.INVISIBLE);
        alreadyFriendsTextView.setVisibility(View.INVISIBLE);


        closeButton.setOnClickListener(v -> {
            dismiss();
            dialog.dismiss();
        });

        goBack.setOnClickListener(v -> dismiss());

        addContact.setOnClickListener(v -> {
            addNewContact();
            Toast.makeText(v.getContext(), "Contact Added", Toast.LENGTH_SHORT).show();
        });

        getOwnerInfo();
    }

    private void addNewContact(){
        SeeUsersFragment addUser = new SeeUsersFragment();
        addUser.becomeFriendWithUser(emailTextView.getText().toString(), mAuth.getUid(), database, false, false);
    }

    private void getOwnerInfo(){
        new Thread(() -> {
            mDatabase.child("Users").child(ownerId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    titleTextView.setText(petName + "'s Owner\nContact Information");
                    emailTextView.setText("Email: " + dataSnapshot.child("Email").getValue().toString());
                    usernameTextView.setText("Username: " + dataSnapshot.child("UsernameText").getValue().toString());

                    if(dataSnapshot.hasChild("Phone")){
                        phoneTextView.setText("Phone: " + dataSnapshot.child("Phone").getValue().toString());
                        phoneTextView.setVisibility(View.VISIBLE);
                    }

                    if(dataSnapshot.hasChild("RealName")){
                        nameTextView.setText("Full Name: " + dataSnapshot.child("RealName").getValue().toString());
                        nameTextView.setVisibility(View.VISIBLE);
                    }

                    Object photoUrlObject = dataSnapshot.child("profilePicURL").getValue();
                    if(photoUrlObject != null) {
                        String photoURL = photoUrlObject.toString();
                        Glide.with(getContext()).load(photoURL).into(image);
                    }

                    image.setVisibility(View.VISIBLE);
                    titleTextView.setVisibility(View.VISIBLE);
                    usernameTextView.setVisibility(View.VISIBLE);
                    emailTextView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);

                    String userId = mAuth.getCurrentUser().getUid();

                    if(userId.equals(ownerId)){
                        reportedByYou.setVisibility(View.VISIBLE);
                    } else if (dataSnapshot.child("Friends").hasChild(userId)) {
                        alreadyFriendsTextView.setVisibility(View.VISIBLE);
                    } else {
                        addContact.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }).start();
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}
