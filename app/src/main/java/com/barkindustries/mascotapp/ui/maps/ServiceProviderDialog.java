package com.barkindustries.mascotapp.ui.maps;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.barkindustries.mascotapp.R;
import com.barkindustries.mascotapp.ui.chatWithVets.SeeUsersFragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class ServiceProviderDialog extends Dialog implements android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;

    private String serviceProviderId;
    private String serviceProviderType;
    private Fragment fragment;

    private ImageView photoImageView, star1, star2, star3, star4, star5;
    private TextView nameTextView, typeTextView, ratingTextView, emailTextView, descriptionTextView, addressTextView, phoneTextView, alreadyFriendsTextView;
    private Button closeButton, addContactButton;
    private ProgressBar progressBar;

    private DatabaseReference mDatabase;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth mAuth;
    private String userId;

    private Boolean isService;



    public ServiceProviderDialog(@NonNull Context context, Fragment fragment, String serviceProviderId, String serviceProviderType, Boolean isService) {
        super(context);
        this.fragment = fragment;
        this.serviceProviderId = serviceProviderId;
        this.serviceProviderType = serviceProviderType;
        this.isService = isService;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.service_provider_dialog);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userId = mAuth.getUid();

        photoImageView = findViewById(R.id.photo_imageView);
        nameTextView = findViewById(R.id.name_textView);
        typeTextView = findViewById(R.id.type_textView);
        ratingTextView = findViewById(R.id.rating_textView);
        emailTextView = findViewById(R.id.email_textView);
        descriptionTextView = findViewById(R.id.description_textView);
        addressTextView = findViewById(R.id.address_textView);
        phoneTextView = findViewById(R.id.aux_textView);
        closeButton = findViewById(R.id.close_button);
        addContactButton = findViewById(R.id.add_contact_provider_btn);
        alreadyFriendsTextView = findViewById(R.id.already_friends_textView);

        progressBar = findViewById(R.id.progressBar);
        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);
        star5 = findViewById(R.id.star5);

        photoImageView.setVisibility(View.INVISIBLE);
        nameTextView.setVisibility(View.INVISIBLE);
        typeTextView.setVisibility(View.INVISIBLE);
        ratingTextView.setVisibility(View.INVISIBLE);
        emailTextView.setVisibility(View.INVISIBLE);
        descriptionTextView.setVisibility(View.INVISIBLE);
        addressTextView.setVisibility(View.INVISIBLE);
        phoneTextView.setVisibility(View.INVISIBLE);
        closeButton.setVisibility(View.INVISIBLE);
        star1.setVisibility(View.INVISIBLE);
        star2.setVisibility(View.INVISIBLE);
        star3.setVisibility(View.INVISIBLE);
        star4.setVisibility(View.INVISIBLE);
        star5.setVisibility(View.INVISIBLE);
        alreadyFriendsTextView.setVisibility(View.INVISIBLE);
        addContactButton.setVisibility(View.GONE);

        closeButton.setOnClickListener(v -> dismiss());

        addContactButton.setOnClickListener(v -> {
            SeeUsersFragment addContact = new SeeUsersFragment();
            addContact.becomeFriendWithUser(emailTextView.getText().toString(), mAuth.getUid(), firebaseDatabase, false, false);
            addContactButton.setVisibility(View.INVISIBLE);
            alreadyFriendsTextView.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Contact Added", Toast.LENGTH_SHORT).show();
        });

        star1.setOnClickListener(v -> starPressed(1));
        star2.setOnClickListener(v -> starPressed(2));
        star3.setOnClickListener(v -> starPressed(3));
        star4.setOnClickListener(v -> starPressed(4));
        star5.setOnClickListener(v -> starPressed(5));

        getInfo();
        

    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    private void starPressed(int rating) {
        if(rating >= 1)
            star1.setImageResource(R.drawable.ic_star_yellow);
        if(rating >= 2)
            star2.setImageResource(R.drawable.ic_star_yellow);
        if(rating >= 3)
            star3.setImageResource(R.drawable.ic_star_yellow);
        if(rating >= 4)
            star4.setImageResource(R.drawable.ic_star_yellow);
        if(rating >= 5)
            star5.setImageResource(R.drawable.ic_star_yellow);

        new Thread(() -> {
            mDatabase.child("Services").child(serviceProviderType).child(serviceProviderId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    long count = (long) dataSnapshot.child("starCounter").getValue() + 1;
                    long starSum = (long) dataSnapshot.child("Stars").getValue() + rating;
                    double newValue = (double) starSum / count;
                    ratingTextView.setText(new DecimalFormat("#.#").format(newValue) + " out of 5");
                    mDatabase.child("Services").child(serviceProviderType).child(serviceProviderId).child("starCounter").setValue(count);
                    mDatabase.child("Services").child(serviceProviderType).child(serviceProviderId).child("Stars").setValue(starSum);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }).start();

    }

    private void getInfo(){


        mDatabase.child("Users").child(userId).child("Friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!isService){
                    if(!snapshot.hasChild(serviceProviderId))
                        addContactButton.setVisibility(View.VISIBLE);
                    else
                        alreadyFriendsTextView.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        new Thread(() -> {
            mDatabase.child("Services").child(serviceProviderType).child(serviceProviderId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    progressBar.setVisibility(View.INVISIBLE);


                    if(dataSnapshot.child("BusinessName").getValue() != null)
                        nameTextView.setText(dataSnapshot.child("BusinessName").getValue().toString());
                    else
                        nameTextView.setText(dataSnapshot.child("UsernameText").getValue().toString());

                    typeTextView.setText(serviceProviderType);

                    double starsAvg = 0.0;
                    long stars = (long) dataSnapshot.child("Stars").getValue();
                    long starsCounter = (long) dataSnapshot.child("starCounter").getValue();
                    if(starsCounter != 0)
                        starsAvg = (double) stars / starsCounter;
                    ratingTextView.setText(new DecimalFormat("#.#").format(starsAvg) + " out of 5");

                    emailTextView.setText(dataSnapshot.child("Email").getValue().toString());

                    if(dataSnapshot.child("Motto").getValue() != null) {
                        descriptionTextView.setText(dataSnapshot.child("Motto").getValue().toString());
                        descriptionTextView.setVisibility(View.VISIBLE);
                    }
                    else
                        descriptionTextView.setVisibility(View.GONE);

                    if(dataSnapshot.child("Phone").getValue() != null) {
                        phoneTextView.setText(dataSnapshot.child("Phone").getValue().toString());
                        phoneTextView.setVisibility(View.VISIBLE);
                    }
                    else
                        phoneTextView.setVisibility(View.GONE);

                    Geocoder geocoder;
                    List<Address> addresses;
                    geocoder = new Geocoder(getContext(), Locale.getDefault());

                    try {
                        addresses = geocoder.getFromLocation((double) dataSnapshot.child("Location").child("lat").getValue(), (double) dataSnapshot.child("Location").child("long").getValue(), 1);
                        addressTextView.setText(addresses.get(0).getAddressLine(0));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Object photoUrlObject = dataSnapshot.child("profilePicURL").getValue();
                    if(photoUrlObject != null) {
                        String photoURL = photoUrlObject.toString();
                        Glide.with(getContext()).load(photoURL).into(photoImageView);
                    }

                    photoImageView.setVisibility(View.VISIBLE);
                    nameTextView.setVisibility(View.VISIBLE);
                    typeTextView.setVisibility(View.VISIBLE);
                    ratingTextView.setVisibility(View.VISIBLE);
                    emailTextView.setVisibility(View.VISIBLE);
                    addressTextView.setVisibility(View.VISIBLE);
                    closeButton.setVisibility(View.VISIBLE);
                    star1.setVisibility(View.VISIBLE);
                    star2.setVisibility(View.VISIBLE);
                    star3.setVisibility(View.VISIBLE);
                    star4.setVisibility(View.VISIBLE);
                    star5.setVisibility(View.VISIBLE);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }).start();
    }



}
